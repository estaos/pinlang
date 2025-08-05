package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class NullExpression extends Expression {
    public NullExpression(Source source) {
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
