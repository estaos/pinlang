package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class WhileLoop extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;
}
