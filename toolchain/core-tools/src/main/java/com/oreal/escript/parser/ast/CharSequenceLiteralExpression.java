package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CharSequenceLiteralExpression extends Expression {
    private CharSequence charSequence;

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
