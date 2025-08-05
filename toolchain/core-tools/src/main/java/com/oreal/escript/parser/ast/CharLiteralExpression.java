package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class CharLiteralExpression extends Expression {
    private char character;

    public CharLiteralExpression(Source source, char character) {
        setSource(source);
        this.character = character;
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
