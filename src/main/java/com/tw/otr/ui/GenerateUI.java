package com.tw.otr.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.tw.otr.component.ConfigState;
import com.tw.otr.component.EntityBuilderService;
import com.tw.otr.notification.MyNotificationGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GenerateUI extends DialogWrapper {
    private Project project;
    private String text;
    private JTextArea jTextArea;

    public GenerateUI(Project project) {
        super(true);
        setTitle("生成entityBuilder");
        this.project=project;
        this.text=readFileOrFindFolder(project);
        init();
    }

    @Override
    protected @NotNull Action getOKAction() {

        generateFile(this.jTextArea.getText());
        return super.getOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel jPanel =new JPanel();
        jPanel.setPreferredSize(new Dimension(300, 150));
        JButton jButton=new JButton("点击选择目录");
        jTextArea = new JTextArea(this.text,6,50);
        jPanel.setLayout(new BorderLayout(1,1));
        jButton.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    project, ProjectUtil.guessProjectDir(project));
            if (virtualFile!=null){
                jTextArea.setText(virtualFile.getPath());
            }
        });
        jPanel.add("East",jButton);
        jPanel.add("West",jTextArea);

        return jPanel ;
    }
    private void generateFile(String pathData) { ;
        File existsFile=new File(pathData);
        if (existsFile.exists()&&existsFile.isFile()){
            MyNotificationGroup.notifyError(project,"路径错误\n"+pathData);
        } else{
            existsFile.mkdirs();
        }
        if (pathData.indexOf('/')==-1){
            return;
        }
        ConfigState configState = EntityBuilderService.getInstance(this.project).getState();
        configState.setPath(pathData);
        EntityBuilderService.getInstance(this.project).loadState(configState);
    }
}
