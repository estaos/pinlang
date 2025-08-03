package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReturnStatement extends Expression {
    private Expression returnExpression;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
