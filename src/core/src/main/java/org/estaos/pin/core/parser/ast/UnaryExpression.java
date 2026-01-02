package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public abstract class UnaryExpression extends Expression {
    private Expression operand;

    @Override
    public boolean isConstExpression() {
        return operand.isConstExpression();
    }
}