package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class IncrementExpression extends Expression {
    private SymbolValueExpression operand;
    private boolean isPreIncrement;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
