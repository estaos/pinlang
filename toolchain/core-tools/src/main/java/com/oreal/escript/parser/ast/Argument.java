package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public class Argument {
    private Expression expression;

    public Argument(Expression expression) {
        this.expression = expression;
    }

    /// Set when passing this argument to a specific parameter.
    private @Nullable String name;
}
