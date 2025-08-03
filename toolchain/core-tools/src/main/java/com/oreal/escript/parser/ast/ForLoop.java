package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ForLoop extends Expression {
    private @Nullable Expression declarationExpression;
    private @Nullable Expression comparisonExpression;
    private @Nullable Expression counterExpression;
    private BlockExpression blockExpression;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
