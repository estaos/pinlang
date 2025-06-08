package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FunctionCallExpression extends Expression {
    private List<Argument> arguments;

    public FunctionCallExpression() {
        super(null);
    }
}
