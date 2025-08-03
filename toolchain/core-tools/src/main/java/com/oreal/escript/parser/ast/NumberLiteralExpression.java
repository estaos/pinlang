package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NumberLiteralExpression extends Expression {
    private String numberAsString;

    public boolean isDecimal() {
        return numberAsString.contains(".")
                || numberAsString.contains("E")
                || numberAsString.contains("e");
    }
}
