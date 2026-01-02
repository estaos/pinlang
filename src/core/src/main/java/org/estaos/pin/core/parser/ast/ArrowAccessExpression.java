package org.estaos.pin.core.parser.ast;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ArrowAccessExpression extends BinaryOperatorExpression {
    public ArrowAccessExpression(Expression left, Expression right) {
        super(left, right);
    }
}