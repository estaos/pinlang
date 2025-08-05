package com.oreal.escript.parser.ast;

public class ContinueStatement extends Expression {
    public ContinueStatement(Source source) {
        super(null);
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
