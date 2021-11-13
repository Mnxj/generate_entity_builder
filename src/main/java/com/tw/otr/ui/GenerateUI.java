package com.tw.otr.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.tw.otr.component.ConfigState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.tw.otr.util.Utils.generateFile;
import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GenerateUI extends DialogWrapper {
    private Project project;
    private ConfigState configState;
    private JTextArea jTextAreaPath;
    private JTextArea jTextAreaPackageName;
    private JLabel jLabelPackageName;
    private JButton jButton;

    public GenerateUI(Project project) {
        super(true);
        setTitle("生成entityBuilder");
        this.project=project;
        this.configState=readFileOrFindFolder(project);
        init();
    }

    @Override
    protected @NotNull Action getOKAction() {
        generateFile(this.project,this.jTextAreaPath.getText().trim(),this.jTextAreaPackageName.getText().trim());
        return super.getOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel jPanel =new JPanel();
        jLabelPackageName=new JLabel("StartPackageName(列：com.intellij -> com)");
        jLabelPackageName.setHorizontalAlignment(SwingConstants.CENTER);
        jLabelPackageName.setPreferredSize(new Dimension(5, 20));
        jTextAreaPackageName = new JTextArea(this.configState.getStartPackageName(),1,20);
        jButton=new JButton("选择目录or输入");
        jButton.setPreferredSize(new Dimension(5, 10));
        jTextAreaPath = new JTextArea(this.configState.getPath(),2,40);

        jButton.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    project, ProjectUtil.guessProjectDir(project));
            if (virtualFile!=null){
                jTextAreaPath.setText(virtualFile.getPath());
            }
        });
        jPanel.add(jLabelPackageName);
        jPanel.add(jTextAreaPackageName);
        jPanel.add(jButton);
        jPanel.add(jTextAreaPath);
        jPanel.setLayout(new GridLayout(2,2,5,5));
        return jPanel ;
    }
}
