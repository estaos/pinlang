package com.oreal.escript.parser.ast;

public class ContinueStatement extends Expression {
    public ContinueStatement() {
        super(null);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
