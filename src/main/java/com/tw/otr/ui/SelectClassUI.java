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

public class SelectClassUI extends DialogWrapper {

    private final Map<String,PsiClassImpl> childClassMap =new HashMap<>();
    private JList<String> list;
    private boolean selectFlag;

    public boolean getSelectFlag(){
        return selectFlag;
    }

    public SelectClassUI(Set<PsiClassImpl> childClass) {
        super(true);
        setTitle("选择需要生成依赖");
        this.selectFlag=false;
        childClass.forEach(child->childClassMap.put(child.toString(),child));
        init();
    }

    @Override
    protected void doOKAction() {
        list.getSelectedValuesList().forEach(childClassMap::remove);
        this.selectFlag=true;
        super.doOKAction();
    }

    public Set<PsiClassImpl> getChildClass() {
        Collection<PsiClassImpl> values = childClassMap.values();
        return new HashSet<>(values);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel contentPane = new JPanel();
        Set<String> strings = childClassMap.keySet();
        contentPane.setLayout(new GridLayoutManager(1, 1, JBUI.insets(10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1));
        contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        list = new JBList<>(strings);
        MyJCheckBox myJCheckBox=new MyJCheckBox();
        list.setCellRenderer(myJCheckBox);
        list.setSelectionModel(new DefaultListSelectionModel() {
            @Override
            public void setSelectionInterval(int index0, int index1) {
                if (super.isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1);
                } else {
                    super.addSelectionInterval(index0, index1);
                }
            }
        });
        panel1.add(list, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        return contentPane;
    }
}
