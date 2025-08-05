package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class BooleanLiteralExpression extends Expression {
    private boolean value;

    public BooleanLiteralExpression(Source source, boolean value) {
        setSource(source);
        this.value = value;
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
