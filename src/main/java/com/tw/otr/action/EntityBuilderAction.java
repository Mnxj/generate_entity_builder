package com.tw.otr.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import static com.tw.otr.util.Utils.readFileOrFindFolder;

public class EntityBuilderAction {
    public static final String IMPORT_VARIABLE = "import ";
    public static final String PUBLIC_VARIABLE = "    public ";
    public static final String JAVA_VARIABLE = "java.";
    private Set<String> generateClassName;
    private Map<String, String> importClassNameMap;
    private Set<PsiClassImpl> childClass;
    private Editor editor;
    private Project project;
    private AnActionEvent event;
    private PsiClassImpl psiClass;
    private StringBuilder buffer;
    private boolean generateFileFlag;

    public EntityBuilderAction(AnActionEvent event, boolean generateFileFlag) {
        this.event = event;
        this.generateFileFlag = generateFileFlag;
        this.startBuild();
    }

    private void startBuild() {
        this.editor = event.getRequiredData(CommonDataKeys.EDITOR);
        this.generateClassName = new HashSet<>();
        this.importClassNameMap = new HashMap<>();
        this.childClass = new HashSet<>();
        this.project = event.getRequiredData(CommonDataKeys.PROJECT);
        PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
        if (psiElement == null) {
            return;
        }
        this.psiClass = (PsiClassImpl) psiElement;
        buffer = new StringBuilder();
    }

    public void generateBuild() {
        generateStart(this.psiClass);
        close();
    }


    private void generateStart(PsiClassImpl psiType) {
        if (generateClassName.contains(psiType.getQualifiedName())) {
            return;
        }
        generateClassName.add(psiType.getQualifiedName());
        String className = psiType.getName();
        String builderClassName = className + "Builder";
        if (className == null) {
            return;
        }
        importClassNameMap.put(psiType.getName(), psiType.getQualifiedName());
        PsiMethod[] parentMethods = psiType.getAllMethods();
        String variableClassName = lowercaseLetter(className);
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        primaryCaret.removeSelection();
        Map<String, String> allReturnType = getReturnType(psiType);

        ConfigState configState = readFileOrFindFolder(project);
        String folderName = configState.getPath();
        if (folderName == null) {
            return;
        }
        String packageName = getPackageName(folderName);
        if (packageName == null) {
            return;
        }
        String classNameRepository = className + "Repository";
        buffer.append("package ").append(packageName).append(";\n\n");
        String convertClassName = lowercaseLetter(className);
        buffer.append(IMPORT_VARIABLE).append(importClassNameMap.get(className)).append(";\n");
        buildImportParams(parentMethods, allReturnType, packageName);
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
        buffer.append(PUBLIC_VARIABLE)
                .append(className)
                .append(" build() {\n        return ")
                .append(convertClassName)
                .append(";\n    }\n\n");
        if (!className.endsWith("DTO")) {
            buffer.append(PUBLIC_VARIABLE)
                    .append(className)
                    .append(" persist() {\n        ")
                    .append(classNameRepository)
                    .append(" repository = SpringApplicationContext.getBean(")
                    .append(classNameRepository)
                    .append(".class);\n        return repository.saveAndFlush(")
                    .append(convertClassName)
                    .append(");\n    }\n\n");
        }
        buildWithParams(builderClassName, parentMethods, variableClassName, allReturnType);
        handleFile(folderName + "/" + builderClassName + ".java",builderClassName);
    }

    private void handleFile( String fileName,String builderClassName) {
        if (!generateFileFlag){
            generateFile(fileName);
        }else{
            newContentToFile(fileName,builderClassName);
        }
    }

    private void generateFile(String fileName){
        File file = new File(fileName);
        try {
            Files.delete(Path.of(fileName));
            file.createNewFile();
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file.getAbsoluteFile()))){
                bufferedWriter.write(buffer.toString());
            }
            MyNotificationGroup.notifyInfo(project, "build成功\n" + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            MyNotificationGroup.notifyError(project, "build失败\n" + fileName + "\nerror:" + e.getMessage());
        }
    }

    private void newContentToFile(String fileName,String builderClassName){
        File file = new File(fileName);
        if (!file.exists()){
            MyNotificationGroup.notifyError(project, "文件不存在\n" + fileName);
        }


    }

    private String getPackageName(String folderName) {
        int com = folderName.indexOf("java");
        if (com < 0) {
            return null;
        }
        return Pattern.compile("/").matcher(folderName.substring(com + JAVA_VARIABLE.length())).replaceAll(".").trim();
    }

    private void buildWithParams(String builderClassName,
                                 PsiMethod[] parentMethods,
                                 String variableClassName,
                                 Map<String, String> allReturnType) {
        for (PsiMethod method : parentMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String filedName = methodName.replace("set", "");
                String convertFiledName = lowercaseLetter(filedName);
                buffer.append(PUBLIC_VARIABLE).append(builderClassName)
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
                                   String packageName) {
        Set<String> importClassNames = new HashSet<>();
        for (PsiMethod method : parentMethods) {
            String methodName = method.getName();
            if (methodName.startsWith("set")) {
                String filedName = methodName.replace("set", "");
                String convertFiledName = lowercaseLetter(filedName);
                if (methodName.startsWith("set")) {
                    Arrays.stream(allReturnType.get(convertFiledName).replaceAll("[<>]", " ").split(" "))
                            .forEach(s -> {
                                if (importClassNameMap.containsKey(s.toLowerCase())) {
                                    importClassNames.add(importClassNameMap.get(s.toLowerCase()));
                                }

                            });
                }
            }
        }

        importClassNames.stream().filter(importClassName -> importClassName.contains(packageName.split("\\.")[0]))
                .forEach(importClassName -> buffer.append(IMPORT_VARIABLE)
                        .append(importClassName).append(";\n"));
        buffer.append("\nimport lombok.AccessLevel;\n" + "import lombok.NoArgsConstructor;\n\n");
        importClassNames.stream().filter(importClassName -> importClassName.contains(".math.")).forEach(importClassName -> buffer.append(IMPORT_VARIABLE)
                .append(importClassName).append(";\n"));
        importClassNames.stream().filter(importClassName -> importClassName.contains(".util.")).forEach(importClassName -> buffer.append(IMPORT_VARIABLE)
                .append(importClassName).append(";\n"));
    }


    private Map<String, String> getReturnType(PsiClassImpl psiClass) {
        Map<String, String> mapFields = new HashMap<>();
        List<PsiField> fields = new ArrayList<>(Arrays.asList(psiClass.getFields()));
        PsiClass superClass = psiClass.getSuperClass();
        while (Objects.nonNull(superClass)) {
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperClass();
        }
        for (PsiField field : fields) {
            mapFields.put(getVariable(field.getName()), field.getType().getPresentableText());
            String canonicalText = field.getType().getCanonicalText();
            canonicalText = getClassName(canonicalText);
            if (!canonicalText.startsWith(JAVA_VARIABLE)) {
                PsiClass aClass = getPsiClass(canonicalText);
                if (aClass == null || childClass.contains(aClass)) {
                    continue;
                }
                generateStart((PsiClassImpl) aClass);
            }
        }
        return mapFields;
    }

    private PsiClass getPsiClass(String canonicalText) {
        return JavaPsiFacade.getInstance(project).findClass(canonicalText,
                GlobalSearchScope.projectScope(project));
    }

    private String getClassName(String canonicalText) {
        if (canonicalText.contains("util") && canonicalText.contains("<")) {
            int i = canonicalText.indexOf("<");
            String substring = canonicalText.substring(0, i);
            String[] split1 = substring.split("\\.");
            importClassNameMap.put(split1[split1.length - 1].toLowerCase(), substring);
            canonicalText = canonicalText.substring(i + 1, canonicalText.length() - 1);
            if (canonicalText.contains("util")) {
                return getClassName(canonicalText);
            }
            String[] split = canonicalText.split(",");
            if (split.length > 1) {
                canonicalText = split[1];
            }
            return getClassName(canonicalText);
        }
        String[] split = canonicalText.split("\\.");
        if (!canonicalText.contains("lang")) {
            importClassNameMap.put(split[split.length - 1].toLowerCase(), canonicalText);
        }
        return canonicalText;
    }

    public void setChildClass(Set<PsiClassImpl> childClass) {
        this.childClass = childClass;
    }

    public Set<PsiClassImpl> getReturnType() {
        List<PsiField> fields = new ArrayList<>(Arrays.asList(this.psiClass.getFields()));
        PsiClass superClass = psiClass.getSuperClass();
        while (Objects.nonNull(superClass)) {
            fields.addAll(Arrays.asList(superClass.getFields()));
            superClass = superClass.getSuperClass();
        }
        for (PsiField field : fields) {
            String canonicalText = field.getType().getCanonicalText();
            canonicalText = getClassName(canonicalText);
            if (!canonicalText.startsWith(JAVA_VARIABLE)) {
                PsiClass aClass = getPsiClass(canonicalText);
                if (aClass == null) {
                    continue;
                }
                childClass.add((PsiClassImpl) aClass);
            }
        }
        return childClass;
    }

    private String getVariable(String name) {
        return name.startsWith("is")
                ? String.valueOf(name.charAt(2)).toLowerCase()+name.substring(3)
                : name;
    }

    private String lowercaseLetter(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    private void close() {
        generateClassName.clear();
        importClassNameMap.clear();
    }
}
