package com.tw.otr.component;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State( name = "entityBuilderPath")
public class EntityBuilderService implements PersistentStateComponent<ConfigState> {
    private ConfigState sparkConfigurationState = new ConfigState();

    @Override
    public @Nullable ConfigState getState() {
        return sparkConfigurationState;
    }

    @Override
    public void loadState(@NotNull ConfigState state) {
        sparkConfigurationState = state;
    }

    public static PersistentStateComponent<ConfigState> getInstance(Project project){
        return ServiceManager.getService(project,EntityBuilderService.class);
    }
}
