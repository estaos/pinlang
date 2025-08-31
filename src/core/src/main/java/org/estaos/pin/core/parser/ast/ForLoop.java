package org.estaos.pin.core.parser.ast;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.Nullable;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForLoop extends Expression {
    private @Nullable Expression declarationExpression;
    private @Nullable Expression comparisonExpression;
    private @Nullable Expression counterExpression;
    private BlockExpression blockExpression;

    public ForLoop(Source source, @Nullable Expression declarationExpression,
                   @Nullable Expression comparisonExpression, @Nullable Expression counterExpression,
                   BlockExpression blockExpression) {
        this.declarationExpression = declarationExpression;
        this.comparisonExpression = comparisonExpression;
        this.counterExpression = counterExpression;
        this.blockExpression = blockExpression;
        setSource(source);
    }

    @Override
    public boolean isConstExpression() {
        return false;
    }
}
