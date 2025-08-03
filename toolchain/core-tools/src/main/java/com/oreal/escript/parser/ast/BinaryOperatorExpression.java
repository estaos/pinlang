package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class BinaryOperatorExpression extends Expression {
    private Expression left;
    private Expression right;

    @Override
    public boolean isConstExpression() {
        return left.isConstExpression() && right.isConstExpression();
    }
}
