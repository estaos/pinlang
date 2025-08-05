package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TypeReference {
    private String name;

    /// This is the actual resolved type being referenced.
    ///
    /// Set during annotation.
    private @Nullable Type type;

    private int arrayDimensions;

    /// Keeps a list of type arguments to this TypeReference.
    ///
    /// For example, given:
    /// ```
    /// val Map<String, int32> a;
    /// ```
    /// The type arguments for this symbol are: `String` and `int32` in that specific order.
    private List<TypeReference> typeArguments;

    public static TypeReference ofType(Type type) {
        return new TypeReference(type.getName(), type, 0, List.of());
    }

    public static TypeReference ofType(Type type, int arrayDimensions) {
        return new TypeReference(type.getName(), type, arrayDimensions, List.of());
    }

    public static TypeReference ofType(String typeName) {
        return new TypeReference(typeName, null, 0, List.of());
    }

    public static TypeReference ofType(String typeName, int arrayDimensions) {
        return new TypeReference(typeName, null, arrayDimensions, List.of());
    }
}
