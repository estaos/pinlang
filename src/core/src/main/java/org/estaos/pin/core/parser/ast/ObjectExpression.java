package org.estaos.pin.core.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class ObjectExpression extends Expression {
    private final List<NamedValueSymbol> namedValues;

    public ObjectExpression(Source source, List<NamedValueSymbol> namedValues) {
        super();
        this.setSource(source);
        this.namedValues = namedValues;
    }

    @Override
    public boolean isConstExpression() {
        return namedValues.stream()
                .allMatch(namedValue ->
                        Objects.requireNonNull(namedValue.getValue()).isConstExpression());
    }
}