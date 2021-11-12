package com.tw.otr.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.tw.otr.notification.MyNotificationGroup;
import com.tw.otr.ui.GenerateUI;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GenerateEntityBuilder extends AnAction {
    private Set<String> generateClassName=new HashSet<>();
    private Map<String,String> importClassNameMap =new HashMap<>();
    @Override
    public void actionPerformed(AnActionEvent event) {
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            return;
        }

        PsiClassImpl psiType = (PsiClassImpl) psiElement;
        final Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        final Project project = event.getRequiredData(CommonDataKeys.PROJECT);

        GenerateUI generateUI = new GenerateUI(project);
        generateUI.show();
        generateStart(psiType, editor, project);
        generateUI.close(DialogWrapper.OK_EXIT_CODE);
        generateClassName.clear();
        importClassNameMap.clear();
    }

    private void generateStart(PsiClassImpl psiType, Editor editor, Project project) {
        if(generateClassName.contains(psiType.getQualifiedName())){
            return;
        }
        generateClassName.add(psiType.getQualifiedName());
        String className = psiType.getName();
        String builderClassName = className + "Builder";
        if (className==null){
            return;
        }
        importClassNameMap.put(psiType.getName(),psiType.getQualifiedName());
        PsiMethod[] parentMethods = psiType.getAllMethods();
        String variableClassName = lowercaseLetter(className);
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        primaryCaret.removeSelection();
        Map<String, String> allReturnType = getALLReturnType(psiType,editor, project);
        StringBuffer buffer = new StringBuffer();
        String folderName = readFileOrFindFolder(project);
        if (folderName == null) {
            return;
        }
        int com = folderName.indexOf("com");
        if (com<0){
            return;
        }
        String packageName=folderName.substring(com).replaceAll("/",".").trim();
        String classNameRepository=className+"Repository";
        buffer.append("package ").append(packageName).append(";\n\n");
        String convertClassName = lowercaseLetter(className);
        buildImportParams(parentMethods, allReturnType, buffer);
        buffer.append("import ").append(importClassNameMap.get(className)).append(";\n");

        buffer.append("\nimport lombok.AccessLevel;\n" +
                "import lombok.NoArgsConstructor;\n\n" +
                "@NoArgsConstructor(access = AccessLevel.PRIVATE)\npublic class ")
                .append(builderClassName).append(" {\n")
                .append("    private ")
                .append(className)
                .append(" ")
                .append(convertClassName)
                .append(" = new ")
                .append(className)
                .append("();\n\n");
        buffer.append("    public static ")
                .append(builderClassName)
                .append(" withDefault() {\n        return new ")
                .append(builderClassName)
                .append("();\n    }\n\n");
        buffer.append("    public ")
                .append(className)
                .append(" build() {\n        return ")
                .append(convertClassName)
                .append(";    \n}\n\n");
        if (!className.endsWith("DTO")){
            buffer.append("    public ")
                    .append(className)
                    .append(" persist() {\n        ")
                    .append(classNameRepository)
                    .append(" repository = SpringApplicationContext.getBean(")
                    .append(classNameRepository)
                    .append(".class);\n        return repository.saveAndFlush(")
                    .append(convertClassName)
                    .append(");\n    }\n\n");
        }
        String fileName= folderName + "/" + builderClassName + ".java";
        buildWithParams(builderClassName, parentMethods, variableClassName, allReturnType, buffer);
        generateFile(buffer, fileName,project);
    }

    private void buildWithParams(String builderClassName, PsiMethod[] parentMethods, String variableClassName, Map<String, String> allReturnType, StringBuffer buffer) {
        for (PsiMethod method : parentMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String filedName = methodName.replace("set", "");
                String convertFiledName = lowercaseLetter(filedName);
                buffer.append("    public ").append(builderClassName)
                        .append(" with").append(filedName)
                        .append("(").append(allReturnType.get(convertFiledName))
                        .append(" ").append(convertFiledName).append(") {\n");

                buffer.append("        ").append(variableClassName)
                        .append(".").append(methodName)
                        .append("(").append(convertFiledName).append(");\n");
                buffer.append("        return this;\n    }\n\n");
            }
        }
        buffer.append("}");
    }

    private void buildImportParams(PsiMethod[] parentMethods, Map<String, String> allReturnType, StringBuffer buffer) {
        Set<String> importClassNames =new HashSet<>();
        for (PsiMethod method : parentMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String filedName = methodName.replace("set", "");
                String convertFiledName = lowercaseLetter(filedName);
                if (methodName.startsWith("set")) {
                    Arrays.stream(allReturnType.get(convertFiledName).replaceAll("[<>]"," ").split(" "))
                            .forEach(s -> {
                                if (importClassNameMap.containsKey(s.toLowerCase())){
                                    importClassNames.add(importClassNameMap.get(s.toLowerCase()));
                                }

                            });
                }
            }
        }
        importClassNames.forEach(importClassName->buffer.append("import ").append(importClassName).append(";\n"));
    }

    private void generateFile(StringBuffer buffer, String fileName,Project project) {
        File file=new File(fileName);
        try {
            file.delete();
            file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            bufferedWriter.write(buffer.toString());
            bufferedWriter.close();
            MyNotificationGroup.notifyInfo(project,"build成功\n"+fileName);
        } catch (IOException e) {
            e.printStackTrace();
            MyNotificationGroup.notifyError(project,"build失败\n"+fileName+"\nerror:"+e.getMessage());
        }
    }

    @NotNull
    private String lowercaseLetter(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private Map<String, String> getALLReturnType(PsiClassImpl psiClass,Editor editor,Project project) {
        Map<String, String> mapFields = new HashMap<>();
        List<PsiField> fields = new ArrayList<>(Arrays.asList(psiClass.getFields()));
        PsiClass superClass = psiClass.getSuperClass();
        while (Objects.nonNull(superClass)){
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperClass();
        }
        for (PsiField field : fields) {
            mapFields.put(field.getName(), field.getType().getPresentableText());
            String canonicalText = field.getType().getCanonicalText();
            canonicalText = getClassName(canonicalText);
            if (!canonicalText.startsWith("java.")){
                PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(canonicalText,
                        GlobalSearchScope.projectScope(project));
                if (aClass ==null){
                    continue;
                }
                generateStart((PsiClassImpl) aClass, editor, project);
            }
        }
        return mapFields;
    }

    private String getClassName(String canonicalText) {
        if (canonicalText.contains("util")&& canonicalText.contains("<")){
            int i = canonicalText.indexOf("<");
            String substring = canonicalText.substring(0, i);
            String[] split1 = substring.split("\\.");
            importClassNameMap.put(split1[split1.length-1].toLowerCase(),substring);
            canonicalText = canonicalText.substring(i+1, canonicalText.length() - 1);
            if (canonicalText.contains("util")){
                return getClassName(canonicalText);
            }
            String[] split = canonicalText.split(",");
            if (split.length>1){
                canonicalText =split[1];
            }
            return getClassName(canonicalText);
        }
        String[] split = canonicalText.split("\\.");
        if (!canonicalText.contains("lang")){
            importClassNameMap.put(split[split.length-1].toLowerCase(),canonicalText);
        }
        return canonicalText;
    }
}
