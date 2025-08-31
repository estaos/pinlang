package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LogicalNotExpression extends Expression {
    private Expression operand;

    public LogicalNotExpression(Source source, Expression operand) {
        this.operand = operand;
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return operand.isConstExpression();
    }
}
