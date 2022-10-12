package com.tw.otr.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogWrapper;
import com.tw.otr.action.EntityBuilderAction;
import com.tw.otr.ui.AnewFileUI;
import com.tw.otr.ui.GeneratePathUI;
import com.tw.otr.ui.SelectClassUI;


public class GenerateEntityBuilder extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        GeneratePathUI generateUI = new GeneratePathUI(event);
        generateUI.show();
        boolean flag = generateUI.getFlag();
        boolean existsFileFlag = generateUI.getExistsFileFlag();
        generateUI.close(DialogWrapper.OK_EXIT_CODE);
        if (flag){
            if (existsFileFlag){
                AnewFileUI  anewFileUI =  new AnewFileUI();
                anewFileUI.show();
                existsFileFlag = anewFileUI.getSelectFlag();

                anewFileUI.close(DialogWrapper.CLOSE_EXIT_CODE);
            }
            EntityBuilderAction entityBuilderAction= new EntityBuilderAction(event,!existsFileFlag);
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
