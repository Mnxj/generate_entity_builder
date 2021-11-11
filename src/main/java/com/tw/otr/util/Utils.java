package com.tw.otr.util;

import com.intellij.openapi.project.Project;
import com.tw.otr.component.EntityBuilderService;

public class Utils {
    public static String readFileOrFindFolder(Project project) {
        return EntityBuilderService.getInstance(project).getState().getPath();
    }
}
