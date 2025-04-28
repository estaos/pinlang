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
    private Source source;
    private String name;
    private List<TypeParameter> typeParameters;

    /// If this type is callable, this is the type of the return value.
    ///
    /// Set to `null` if this type is not callable.
    private @Nullable TypeReference yieldType;

    /// If this type is callable, these are the parameters it should be called with.
    ///
    /// It is the number and type of parameters that differentiate callable types
    /// of the same name. This allows for method/function overloading. To prevent
    /// clashes, for overloaded types, the `fullyQualifiedName` of a Type also
    /// includes its line number and column in the source file.
    private List<Symbol> parameters;

    private String documentationMarkdown;
}