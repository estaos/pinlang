package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class IfStatement extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;
    private @Nullable BlockExpression elseBlockExpression;
    private List<ElseIfBlock> elseIfBlocks;

    @Override
    public boolean isConstExpression() {
        return false;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ElseIfBlock {
        private Expression booleanExpression;
        private BlockExpression blockExpression;
    }
}
