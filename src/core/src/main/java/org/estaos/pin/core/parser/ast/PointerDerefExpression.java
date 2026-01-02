package org.estaos.pin.core.parser.ast;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class PointerDerefExpression extends UnaryExpression {
    public PointerDerefExpression(Source source, Expression operand) {
        super(operand);
        setSource(source);
    }
}