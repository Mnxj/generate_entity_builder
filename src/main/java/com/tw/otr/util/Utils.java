package com.tw.otr.util;

import com.intellij.openapi.project.Project;
import com.tw.otr.component.ConfigState;
import com.tw.otr.component.EntityBuilderService;
import com.tw.otr.notification.MyNotificationGroup;

import java.io.File;

public class Utils {
    public static ConfigState readFileOrFindFolder(Project project) {
        return EntityBuilderService.getInstance(project).getState();
    }
    public  static void generateFile(Project project,String pathData) { ;
        File existsFile=new File(pathData);
        if (existsFile.exists()&&existsFile.isFile()){
            MyNotificationGroup.notifyError(project,"路径错误\n"+pathData);
        } else{
            existsFile.mkdirs();
        }
        if (pathData.indexOf('/')==-1){
            return;
        }
        ConfigState configState = EntityBuilderService.getInstance(project).getState();
        configState.setPath(pathData);
        EntityBuilderService.getInstance(project).loadState(configState);
    }
    public static String lowercaseLetter(String className) {
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }
    public static String getVariable(String name) {
        return name.startsWith("is")
                ? String.valueOf(name.charAt(2)).toLowerCase()+name.substring(3)
                : name;
    }
}
