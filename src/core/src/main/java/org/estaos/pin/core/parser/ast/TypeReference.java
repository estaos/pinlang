package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TypeReference {

    /// Set by the programme.
    ///
    /// Not used during type checking as the resolved type ill have its name.
    private String name;

    /// This is the actual resolved type being referenced.
    ///
    /// Set during annotation.
    private @Nullable Type type;

    /// Set by the programmer.
    ///
    /// Not used during type checking as the resolved type will already be a Pointer.
    ///
    /// Entries in this list are nullable. A null entry basically means that dimension does not have a size yet.
    private List<Expression> arrayDimensions;

    /// Keeps a list of type arguments to this TypeReference.
    ///
    /// For example, given:
    /// ```
    /// val Map<String, int32> a;
    /// ```
    /// The type arguments for this symbol are: `String` and `int32` in that specific order.
    private List<TypeReference> typeArguments;

    public static TypeReference ofType(Type type) {
        return new TypeReference(type.getName(), type, List.of(), List.of());
    }

    public static TypeReference ofType(Type type, List<Expression> arrayDimensions) {
        return new TypeReference(type.getName(), type, arrayDimensions, List.of());
    }

    public static TypeReference ofType(String typeName) {
        return new TypeReference(typeName, null, List.of(), List.of());
    }

    public static TypeReference ofType(String typeName, List<Expression> arrayDimensions) {
        return new TypeReference(typeName, null, arrayDimensions, List.of());
    }
}
