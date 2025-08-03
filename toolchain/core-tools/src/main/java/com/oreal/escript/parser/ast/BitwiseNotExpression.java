package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BitwiseNotExpression extends Expression {
    private Expression operand;
    @Override
    public boolean isConstExpression() {
        return operand.isConstExpression();
    }
}
