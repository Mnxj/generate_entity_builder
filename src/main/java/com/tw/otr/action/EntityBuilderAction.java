package com.tw.otr.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.tw.otr.component.ConfigState;
import com.tw.otr.notification.MyNotificationGroup;

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

import static com.tw.otr.util.Utils.lowercaseLetter;
import static com.tw.otr.util.Utils.readFileOrFindFolder;

public class EntityBuilderAction {
    private Set<String> generateClassName;
    private Map<String,String> importClassNameMap;
    private Set<PsiClassImpl> childClass;
    private Editor editor;
    private Project project;
    private AnActionEvent event;
    private PsiClassImpl psiClass;
    public EntityBuilderAction(AnActionEvent event){
        this.event=event;
        this.startBuild();
    }
    private void startBuild(){
        this.editor=event.getRequiredData(CommonDataKeys.EDITOR);
        this.generateClassName=new HashSet<>();
        this.importClassNameMap =new HashMap<>();
        this.childClass=new HashSet<>();
        this.project=event.getRequiredData(CommonDataKeys.PROJECT);
        PsiElement psiElement = event.getData(LangDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            return;
        }
        this.psiClass = (PsiClassImpl) psiElement;
    }

    public void generateBuild(){
        generateStart(this.psiClass);
    }


    private void generateStart(PsiClassImpl psiType) {
        if(generateClassName.contains(psiType.getQualifiedName())){
            return;
        }
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
        Map<String, String> allReturnType = getReturnType(psiType);
        StringBuffer buffer = new StringBuffer();
        ConfigState configState = readFileOrFindFolder(project);
        String startPackageName = configState.getStartPackageName();
        String folderName = configState.getPath();
        if (folderName == null) {
            return;
        }
        int com = folderName.indexOf(startPackageName);
        if (com<0){
            return;
        }
        String packageName=folderName.substring(com).replaceAll("/",".").trim();
        String classNameRepository=className+"Repository";
        buffer.append("package ").append(packageName).append(";\n\n");
        String convertClassName = lowercaseLetter(className);
        buffer.append("import ").append(importClassNameMap.get(className)).append(";\n");
        buildImportParams(parentMethods, allReturnType, buffer,startPackageName);
        buffer.append("\n@NoArgsConstructor(access = AccessLevel.PRIVATE)\npublic class ")
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
        generateFile(buffer, fileName);
    }

    private void buildWithParams(String builderClassName,
                                 PsiMethod[] parentMethods,
                                 String variableClassName,
                                 Map<String, String> allReturnType,
                                 StringBuffer buffer) {
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

    private void buildImportParams(PsiMethod[] parentMethods,
                                   Map<String, String> allReturnType,
                                   StringBuffer buffer,
                                   String startPackageName) {
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
        importClassNames.stream().filter(importClassName->importClassName.contains(startPackageName)).forEach(importClassName->buffer.append("import ")
                .append(importClassName).append(";\n"));
        buffer.append("\nimport lombok.AccessLevel;\n" + "import lombok.NoArgsConstructor;\n\n");
        importClassNames.stream().filter(importClassName->importClassName.contains(".math.")).forEach(importClassName->buffer.append("import ")
                .append(importClassName).append(";\n"));
        importClassNames.stream().filter(importClassName->importClassName.contains(".util.")).forEach(importClassName->buffer.append("import ")
                .append(importClassName).append(";\n"));
    }

    private void generateFile(StringBuffer buffer,
                              String fileName) {
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


    private Map<String, String> getReturnType(PsiClassImpl psiClass) {
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
                if (aClass ==null||childClass.contains(aClass)){
                    continue;
                }
                generateStart((PsiClassImpl)aClass);
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

    public void setChildClass(Set<PsiClassImpl> childClass) {
        this.childClass = childClass;
    }

    public  Set<PsiClassImpl> getReturnType() {
        List<PsiField> fields = new ArrayList<>(Arrays.asList(this.psiClass.getFields()));
        PsiClass superClass = psiClass.getSuperClass();
        while (Objects.nonNull(superClass)){
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperClass();
        }
        for (PsiField field : fields) {
            String canonicalText = field.getType().getCanonicalText();
            canonicalText = getClassName(canonicalText);
            if (!canonicalText.startsWith("java.")){
                PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(canonicalText,
                        GlobalSearchScope.projectScope(project));
                if (aClass ==null){
                    continue;
                }
                childClass.add((PsiClassImpl) aClass);
            }
        }
        return childClass;
    }
    public void close(){
        generateClassName.clear();
        importClassNameMap.clear();
    }
}
