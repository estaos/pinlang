package com.oreal.escript.parser.ast;

public class BreakStatement extends Expression {
    public BreakStatement() {
        super(null);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
