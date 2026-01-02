package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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

    public boolean isFloat() {
        return numberAsString.contains("f")
                || numberAsString.contains("F");
    }

    public boolean isDouble() {
        return isDecimal() && !isFloat();
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
