package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class NumberLiteralExpression extends Expression {
    private String numberAsString;

    public NumberLiteralExpression(Source source, String numberAsString) {
        setSource(source);
        this.numberAsString = numberAsString;
    }

    public boolean isDecimal() {
        return numberAsString.contains(".")
                || numberAsString.contains("E")
                || numberAsString.contains("e");
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
