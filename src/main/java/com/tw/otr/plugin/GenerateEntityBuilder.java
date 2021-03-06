package com.tw.otr.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.DialogWrapper;
import com.tw.otr.action.EntityBuilderAction;
import com.tw.otr.ui.GeneratePathUI;
import com.tw.otr.ui.SelectClassUI;


public class GenerateEntityBuilder extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        GeneratePathUI generateUI = new GeneratePathUI(event.getRequiredData(CommonDataKeys.PROJECT));
        generateUI.show();
        boolean flag = generateUI.getFlag();
        generateUI.close(DialogWrapper.OK_EXIT_CODE);
        if (flag){
            EntityBuilderAction entityBuilderAction= new EntityBuilderAction(event,generateUI.getGenerateFileFlag());
            SelectClassUI selectClassUI=new SelectClassUI(entityBuilderAction.getReturnType());
            selectClassUI.show();
            boolean selectFlag = selectClassUI.getSelectFlag();
            if (selectFlag){
                entityBuilderAction.setChildClass(selectClassUI.getChildClass());
                entityBuilderAction.generateBuild();
            }
            selectClassUI.close(DialogWrapper.OK_EXIT_CODE);
        }
    }
}
