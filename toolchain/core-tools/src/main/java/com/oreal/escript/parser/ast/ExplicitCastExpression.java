package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class ExplicitCastExpression extends Expression {
    private Expression operand;

    public ExplicitCastExpression(Source source, Expression operand, TypeReference toType) {
        super(toType, source);
        this.operand = operand;
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
