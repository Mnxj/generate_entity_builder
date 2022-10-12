package com.tw.otr.ui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.ui.components.JBList;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import com.tw.otr.component.MyJCheckBox;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AnewFileUI extends DialogWrapper {

    private boolean selectFlag;

    public boolean getSelectFlag(){
        return selectFlag;
    }

    public AnewFileUI() {
        super(true);
        setTitle("是否保留原文件");
        this.selectFlag = false;
        init();
    }

    @Override
    protected void doOKAction() {
        this.selectFlag=true;
        super.doOKAction();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel contentPane = new JPanel();
        JTextArea jTextArea = new JTextArea();
        jTextArea.append("你想要操作的文件已存在，是否要保留mock数据?");
        contentPane.add(jTextArea);
        return contentPane;
    }
}
