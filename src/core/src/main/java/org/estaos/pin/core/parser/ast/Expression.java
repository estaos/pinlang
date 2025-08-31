package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class Expression {
    /// The type this expression evaluates to.
    ///
    /// For block expressions, the un-annotated type is set
    /// during parsing. For all other expressions, the type is
    /// set during annotation (i.e what they evaluate to).
    ///
    /// Void expressions will always have this as null.
    private @Nullable TypeReference type;

    private Source source;

    public Expression(@Nullable TypeReference type) {
        this.type = type;
    }

    public abstract boolean isConstExpression();
}
