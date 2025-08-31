package org.estaos.pin.core.parser.ast;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/// Type pass expressions are void expressions. This means the only way to use them
/// is when they are passed via the variable args part of a function.
public class TypePassExpression extends Expression {
    private TypeReference typeReference;
    public TypePassExpression(Source source, TypeReference typeReference) {
        super.setSource(source);
        this.typeReference = typeReference;
    }
    @Override
    public boolean isConstExpression() {
        return false;
    }
}
