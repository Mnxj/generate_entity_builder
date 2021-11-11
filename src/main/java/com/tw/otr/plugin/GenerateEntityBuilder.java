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
import com.intellij.ui.CollectionListModel;
import com.tw.otr.notification.MyNotificationGroup;
import com.tw.otr.ui.GenerateUI;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GenerateEntityBuilder extends AnAction {
    private Set<String> generateClassName=new HashSet<>();
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
        PsiMethod[] parentMethods = psiType.getAllMethods();
        String variableClassName = lowercaseLetter(className);
        // final Document document = editor.getDocument();
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
//        int start = primaryCaret.getVisualLineEnd();
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
        String packageName=folderName.substring(com).replaceAll("/",".");
        String classNameRepository=className+"Repository";
        buffer.append("package ").append(packageName).append(";\n\n");
        String convertClassName = lowercaseLetter(className);
        buffer.append("import lombok.AccessLevel;\n" +
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
        generateFile(buffer, fileName,project);
//        WriteCommandAction.runWriteCommandAction(project, () ->
//                document.insertString(start, buffer.toString())
//        );
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
        List<PsiField> fields = new CollectionListModel<>(psiClass.getFields()).getItems();
        for (PsiField field : fields) {
            mapFields.put(field.getName(), field.getType().getPresentableText());
            if (!field.getType().getCanonicalText().startsWith("java.")){
                PsiClass aClass = JavaPsiFacade.getInstance(project).findClass(field.getType().getCanonicalText(), GlobalSearchScope.projectScope(project));

                if (aClass ==null){
                    continue;
                }
                generateStart((PsiClassImpl) aClass, editor, project);
            }
        }
        return mapFields;
    }
}
