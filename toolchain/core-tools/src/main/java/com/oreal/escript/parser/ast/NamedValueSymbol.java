package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;

/// For anything that can be treated as a value and that value can be accessed through the symbol name.
/// This includes variables, constants, anonymous functions and structs, etc.
@Getter
@Setter
public class NamedValueSymbol extends Symbol {
    public NamedValueSymbol(String name, TypeReference type, Source source, boolean reAssignable, boolean immutable, String documentationMarkdown, Expression value, boolean overrides) {
        super(name, type, source, documentationMarkdown);
        this.reAssignable = reAssignable;
        this.immutable = immutable;
        this.value = value;
        this.overrides = overrides;
    }

    /**
     * The identifier can be re-assigned.
     */
    private boolean reAssignable;

    /**
     * The value contained within this identifier cannot be changed.
     */
    private boolean immutable;

    private Expression value;

    /// Set to true if this symbol should shadow same symbol in super class.
    private boolean overrides;
}
