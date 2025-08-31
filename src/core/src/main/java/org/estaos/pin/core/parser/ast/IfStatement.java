package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class IfStatement extends Expression {
    private Expression booleanExpression;
    private BlockExpression blockExpression;
    private @Nullable BlockExpression elseBlockExpression;
    private List<ElseIfBlock> elseIfBlocks;

    public IfStatement(Source source, Expression booleanExpression,
                       BlockExpression blockExpression,
                       @Nullable BlockExpression elseBlockExpression,
                       List<ElseIfBlock> elseIfBlocks) {
        setSource(source);
        this.booleanExpression = booleanExpression;
        this.blockExpression = blockExpression;
        this.elseBlockExpression = elseBlockExpression;
        this.elseIfBlocks = elseIfBlocks;
    }

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
