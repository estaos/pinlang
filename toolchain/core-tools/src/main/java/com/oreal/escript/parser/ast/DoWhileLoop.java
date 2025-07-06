package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
public class DoWhileLoop extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;
}
