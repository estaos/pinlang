package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

/// For anything that can be treated as a value and that value can be accessed through the symbol name.
/// This includes variables, constants, anonymous functions and structs, etc.
@Getter
public class NamedValueSymbol extends Symbol {
    public NamedValueSymbol(String name, String fullyQualifiedName, TypeReference type, Source source, boolean reAssignable, boolean immutable) {
        super(name, fullyQualifiedName, type, source);
        this.reAssignable = reAssignable;
        this.immutable = immutable;
    }

    /**
     * The identifier can be re-assigned.
     */
    private final boolean reAssignable;

    /**
     * The value contained within this identifier cannot be changed.
     */
    private final boolean immutable;

}
