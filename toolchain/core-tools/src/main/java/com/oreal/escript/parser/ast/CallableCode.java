package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CallableCode {
    private String name;
    private Source source;
    private CallableType type;
    private BlockExpression statementBlock;
}
