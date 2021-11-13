package com.tw.otr.component;

import com.intellij.util.xmlb.annotations.OptionTag;

public class ConfigState {
    @OptionTag
    private  String path;
    @OptionTag
    private  String startPackageName;

    public String getStartPackageName() {
        return startPackageName;
    }

    public void setStartPackageName(String startPackageName) {
        this.startPackageName = startPackageName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
