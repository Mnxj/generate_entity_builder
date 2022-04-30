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
import com.tw.otr.component.EntityBuilderService;
import com.tw.otr.notification.MyNotificationGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;

import static com.tw.otr.util.Utils.readFileOrFindFolder;


public class GeneratePathUI extends DialogWrapper {
    private final Project project;
    private ConfigState configState;
    private JTextArea jTextAreaPath;
    private boolean flag;
    boolean generateFileFlag;
    private JButton jButton;

    public GeneratePathUI(Project project) {
        super(true);
        setTitle("生成entityBuilder");
        this.project=project;
        this.configState=readFileOrFindFolder(project);
        init();
    }

    @Override
    protected void doOKAction() {
        generatePath(this.jTextAreaPath.getText().trim());
        this.flag=true;
        super.doOKAction();
    }

    private void generatePath(String pathData) {
        File existsPath=new File(pathData);
        if (existsPath.exists()&&existsPath.isFile()){
            MyNotificationGroup.notifyError(this.project,"路径错误\n"+pathData);
        } else{
            existsPath.mkdirs();
        }
        if (pathData.indexOf('/')==-1){
            return;
        }
        this.configState = EntityBuilderService.getInstance(this.project).getState();
        this.configState.setPath(pathData);
        EntityBuilderService.getInstance(this.project).loadState(this.configState);
    }

    public boolean getFlag() {
        return flag;
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return getJPanel();
    }

    @NotNull
    private JPanel getJPanel() {
        JPanel jPanel =new JPanel();
        jPanel.setLayout(new GridLayoutManager(4, 1, JBUI.insets(10), -1, -1));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 2, JBUI.emptyInsets(), -1, -1));
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

        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 3, JBUI.emptyInsets(), -1, -1));
        jPanel.add(panel5, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer = new Spacer();
        panel5.add(spacer, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        panel5.add(panel6, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        JRadioButton again = new JRadioButton();
        again.setText("重新生成文件");
        again.addActionListener(l -> this.generateFileFlag=again.isSelected());
        panel6.add(again, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

        return jPanel;
    }


    public boolean getGenerateFileFlag() {
        return generateFileFlag;
    }
}
