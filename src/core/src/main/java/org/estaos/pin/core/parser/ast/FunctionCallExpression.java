package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FunctionCallExpression extends Expression {
    private List<Argument> arguments;
    private Expression callableExpression;

    public FunctionCallExpression(Source source, List<Argument> arguments, Expression callableExpression) {
        setSource(source);
        this.arguments = arguments;
        this.callableExpression = callableExpression;
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
