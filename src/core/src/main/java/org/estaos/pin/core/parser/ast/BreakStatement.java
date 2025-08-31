package org.estaos.pin.core.parser.ast;

public class BreakStatement extends Expression {
    public BreakStatement(Source source) {
        super(null);
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
