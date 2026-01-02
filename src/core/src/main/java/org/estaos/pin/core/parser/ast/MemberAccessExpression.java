package org.estaos.pin.core.parser.ast;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class MemberAccessExpression extends BinaryOperatorExpression {
    public MemberAccessExpression(Expression left, Expression right) {
        super(left, right);
    }
}