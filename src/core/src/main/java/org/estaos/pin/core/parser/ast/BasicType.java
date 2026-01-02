package org.estaos.pin.core.parser.ast;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BasicType extends Type {
    public BasicType(
            @Nullable Source source,
            String name,
            String documentationMarkdown) {
        super(source, name, List.of(), documentationMarkdown, List.of());
    }

    @Override
    public boolean canBeCastedTo(Type other) {
        return false;
    }

    @Override
    public boolean canBeAssigned(Type other) {
        return false;
    }
}