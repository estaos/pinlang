package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Symbol {
    private final String name;
    private final String fullyQualifiedName;

    private final TypeReference type;
    private final Source source;
}
