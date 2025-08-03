package com.oreal.escript.parser.ast;

/// A special expression that is simply replaced with the name of the datatype
/// of the variable it is assigned to.
public class TypeNameExpression extends Expression {
    public TypeNameExpression(TypeReference type) {
        super(type);
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
