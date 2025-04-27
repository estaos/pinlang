package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@AllArgsConstructor
public class Type {
    private final Source source;
    private final String name;
    private final List<TypeParameter> typeParameters;

    /// If this type is callable, this is the type of the return value.
    ///
    /// Set to `null` if this type is not callable.
    private final @Nullable TypeReference yieldType;

    /// If this type is callable, these are the parameters it should be called with.
    ///
    /// It is the number and type of parameters that differentiates callable types
    /// of the same name. This allows for method/function overloading. To prevent
    /// clashes, the `fullyQualifiedName` of a Type also includes its line number
    /// and column in the source file.
    private final List<Symbol> parameters;
}
