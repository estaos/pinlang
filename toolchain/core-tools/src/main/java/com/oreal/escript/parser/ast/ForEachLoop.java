package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ForEachLoop extends Expression {
    private NamedValueSymbol iteratorAndSource;
    private BlockExpression blockExpression;

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
