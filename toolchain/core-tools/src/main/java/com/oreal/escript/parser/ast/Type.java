package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Type {
    /// Where this type is defined. Is null for built-in types.
    private @Nullable Source source;
    private String name;
    private List<TypeParameter> typeParameters;
    private String documentationMarkdown;
}
