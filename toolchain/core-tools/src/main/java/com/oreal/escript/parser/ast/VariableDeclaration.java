package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VariableDeclaration extends Expression {
    private NamedValueSymbol namedValueSymbol;

    public VariableDeclaration(Source source, NamedValueSymbol namedValueSymbol) {
        super.setSource(source);
        this.namedValueSymbol = namedValueSymbol;
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
