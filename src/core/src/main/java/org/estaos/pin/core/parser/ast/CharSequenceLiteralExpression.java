package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CharSequenceLiteralExpression extends Expression {
    private CharSequence charSequence;

    public CharSequenceLiteralExpression(Source source, CharSequence charSequence) {
        setSource(source);
        this.charSequence = charSequence;
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
