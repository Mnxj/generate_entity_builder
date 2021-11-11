package com.tw.otr.component;

import com.intellij.util.xmlb.annotations.OptionTag;

public class ConfigState {
    @OptionTag
    private  String path;

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
