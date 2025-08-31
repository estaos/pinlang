package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ReturnStatement extends Expression {
    private Expression returnExpression;

    public ReturnStatement(Source source, Expression returnExpression) {
        setSource(source);
        this.returnExpression = returnExpression;
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
