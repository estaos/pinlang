package org.estaos.pin.core.parser.ast;

import lombok.Data;

@Data
public class WhileLoop extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;

    public WhileLoop(Source source, Expression booleanExpression, BlockExpression blockExpression) {
        this.booleanExpression = booleanExpression;
        this.blockExpression = blockExpression;
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
