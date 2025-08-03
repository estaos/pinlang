package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SymbolValueExpression extends Expression {
    private String symbolName;

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
