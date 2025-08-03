package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@Data
public class DecrementExpression extends Expression {
    private SymbolValueExpression operand;
    private boolean isPreDecrement;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
