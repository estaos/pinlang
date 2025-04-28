package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
@AllArgsConstructor
public class Import {
    private @Nullable String namespace;
    private boolean isExternal;

    /// The compilation resulting from parsing this import.
    ///
    /// If the import could not be resolved, the compilation
    /// unit will not be set, i.e `null`.
    private @Nullable CompilationUnit compilationUnit;
}
