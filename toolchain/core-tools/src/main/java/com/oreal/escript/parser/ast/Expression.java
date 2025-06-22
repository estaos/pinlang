package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
@Setter
public class Expression {
    /// The type this expression evaluates to.
    ///
    /// For block expressions, the un-annotated type is set
    /// during parsing. For all other expressions, the type is
    /// set during annotation (i.e what they evaluate to).
    ///
    /// Void expressions will always have this as null.
    private @Nullable TypeReference type;
}
