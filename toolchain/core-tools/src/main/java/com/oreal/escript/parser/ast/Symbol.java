package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Symbol {
    private final String name;

    private final TypeReference type;

    /// This is used to find this symbol in the symbols table.
    private final Source source;

    private String documentationMarkdown;
}
