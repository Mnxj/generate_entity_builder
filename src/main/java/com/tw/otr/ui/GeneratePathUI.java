package com.tw.otr.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;
import com.tw.otr.component.ConfigState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

import static com.tw.otr.util.Utils.generateFile;
import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GeneratePathUI extends DialogWrapper {
    private Project project;
    private ConfigState configState;
    private JTextArea jTextAreaPath;
    private JTextArea jTextAreaPackageName;
    private boolean flag;

    private JButton jButton;

    public GeneratePathUI(Project project) {
        super(true);
        setTitle("生成entityBuilder");
        this.project=project;
        this.configState=readFileOrFindFolder(project);
        init();
    }

    @Override
    protected @NotNull Action getOKAction() {
        generateFile(this.project,this.jTextAreaPath.getText().trim(),this.jTextAreaPackageName.getText().trim());
        this.flag=true;
        return super.getOKAction();
    }

    public Boolean getFlag() {
        return flag;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return getJPanel();
    }

    @NotNull
    private JPanel getJPanel() {
        JPanel jPanel =new JPanel();
        jPanel.setLayout(new GridLayoutManager(3, 1, JBUI.insets(10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, JBUI.emptyInsets(), -1, -1));
        jPanel.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        jPanel.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel jLabelPackageName=new JLabel("StartPackageName(列：com.intellij -> com)");
        panel3.add(jLabelPackageName, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jTextAreaPackageName = new JTextArea(this.configState.getStartPackageName(),1,20);
        panel3.add(jTextAreaPackageName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        jPanel.add(panel4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        jTextAreaPath = new JTextArea(this.configState.getPath(),2,40);
        panel4.add(jTextAreaPath, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        jButton=new JButton("选择目录or输入");
        panel4.add(jButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        jButton.addActionListener(e -> {
            VirtualFile virtualFile = FileChooser.chooseFile(
                    FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    project, ProjectUtil.guessProjectDir(project));
            if (virtualFile!=null){
                jTextAreaPath.setText(virtualFile.getPath());
            }
        });
        return jPanel;
    }
}
