package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class DecrementExpression extends Expression {
    private SymbolValueExpression operand;
    private boolean isPreDecrement;
}
