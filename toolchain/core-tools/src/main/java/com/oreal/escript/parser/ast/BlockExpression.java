package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Setter
@Getter
public class BlockExpression extends Expression {
    private List<Expression> statements;

    public BlockExpression(List<Expression> statements) {
        this.statements = statements;
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
