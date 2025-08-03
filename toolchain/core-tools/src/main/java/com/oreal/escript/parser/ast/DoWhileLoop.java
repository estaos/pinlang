package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class DoWhileLoop extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
