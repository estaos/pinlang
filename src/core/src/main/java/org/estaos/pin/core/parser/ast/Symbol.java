package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class Symbol {
    private final String name;

    private TypeReference type;

    /// This is used to find this symbol in the symbols table.
    private final Source source;

    private String documentationMarkdown;
}
