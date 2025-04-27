package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class Import {
    private final @Nullable String namespace;
    private final boolean isExternal;
    private final CompilationUnit compilationUnit;
}
