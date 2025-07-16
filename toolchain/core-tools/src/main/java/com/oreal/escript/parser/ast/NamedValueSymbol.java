package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/// For anything that can be treated as a value and that value can be accessed through the symbol name.
/// This includes variables, constants, anonymous functions and structs, etc.
@Getter
@Setter
public class NamedValueSymbol extends Symbol {
    public NamedValueSymbol(String name,
                            TypeReference type,
                            Source source,
                            boolean reAssignable,
                            boolean immutable,
                            String documentationMarkdown,
                            @Nullable Expression value,
                            boolean overrides) {
        this(name, type, source, reAssignable, immutable, documentationMarkdown, value, overrides, 0);
    }

    public NamedValueSymbol(String name,
                            TypeReference type,
                            Source source,
                            boolean reAssignable,
                            boolean immutable,
                            String documentationMarkdown,
                            @Nullable Expression value,
                            boolean overrides,
                            int arrayDimensions) {
        super(name, type, source, documentationMarkdown);
        this.reAssignable = reAssignable;
        this.immutable = immutable;
        this.value = value;
        this.overrides = overrides;
        this.arrayDimensions = arrayDimensions;
    }

    /**
     * The identifier can be re-assigned.
     */
    private boolean reAssignable;

    /**
     * The value contained within this identifier cannot be changed.
     */
    private boolean immutable;

    private @Nullable Expression value;

    /// Set to true if this symbol should shadow same symbol in super class.
    private boolean overrides;

    private int arrayDimensions = 0;

    public boolean isArray() {
        return arrayDimensions > 0;
    }

    public boolean isFunction() {
        if(getType().getType() != null) {
            return getType().getType() instanceof CallableType;
        } else {
            return false;
        }
    }
}
