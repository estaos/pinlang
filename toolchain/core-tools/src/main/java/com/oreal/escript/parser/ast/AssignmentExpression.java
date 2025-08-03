package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class AssignmentExpression extends Expression {
    private String symbolName;
    private Expression value;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
