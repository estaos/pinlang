package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TypeReference {
    private String name;
    private String fullyQualifiedName;

    /// Keeps a list of type arguments to this TypeReference.
    ///
    /// For example, given:
    /// ```
    /// val Map<String, int32> a;
    /// ```
    /// The type arguments for this symbol are: `String` and `int32` in that specific order.
    private List<TypeReference> typeArguments;
}
