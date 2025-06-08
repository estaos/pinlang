package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@AllArgsConstructor
public class Import {
    private @Nullable String namespace;
    private boolean isExternal;

    /// Where this import is defined.
    private Source source;

    /// The compilation unit resulting from parsing this import.
    ///
    /// If the import could not be resolved, the compilation
    /// unit will not be set, i.e `null`.
    private @Nullable CompilationUnit compilationUnit;

    public static Import fromSource(Source source) {
        return new Import(null, false, source, null);
    }
}
