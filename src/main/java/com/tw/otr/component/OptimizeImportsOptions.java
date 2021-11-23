package com.tw.otr.component;

import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.Nullable;

public interface OptimizeImportsOptions {
    @Nullable
    SearchScope getSearchScope();
}
