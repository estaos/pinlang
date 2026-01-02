package org.estaos.pin.core.parser.ast;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class AddressOfExpression extends UnaryExpression {
    public AddressOfExpression(Source source, Expression operand) {
        super(operand);
        setSource(source);
    }
}