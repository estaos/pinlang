package org.estaos.pin.core.parser.ast;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class ArrayAccessExpression extends BinaryOperatorExpression {
    public ArrayAccessExpression(Expression left, Expression right) {
        super(left, right);
    }
}