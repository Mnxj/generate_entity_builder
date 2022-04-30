package com.tw.otr.util;

import com.intellij.openapi.project.Project;
import com.tw.otr.component.ConfigState;
import com.tw.otr.component.EntityBuilderService;

public class Utils {

    private Utils(){

    }

    public static ConfigState readFileOrFindFolder(Project project) {
        return EntityBuilderService.getInstance(project).getState();
    }

}
