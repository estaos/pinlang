package com.oreal.escript.parser.ast;

import lombok.Getter;

/// A special expression that points to callable code.
///
/// These are created by the parser hence the callable code is known upfront.
///
/// In C, these expressions return void* to a function.
@Getter
public class CallableCodeExpression extends Expression {
    private final CallableCode callableCode;
    public CallableCodeExpression(Source source, CallableCode callableCode) {
        super(TypeReference.ofType(callableCode.getType()));
        this.callableCode = callableCode;
    }

    @Override
    public boolean isConstExpression() {
        return true;
    }
}
