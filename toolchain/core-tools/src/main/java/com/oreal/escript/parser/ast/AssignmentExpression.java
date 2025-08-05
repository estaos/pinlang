package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AssignmentExpression extends Expression {
    private String symbolName;
    private Expression value;

    public AssignmentExpression(Source source, String symbolName, Expression value) {
        setSource(source);
        this.symbolName = symbolName;
        this.value = value;
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
