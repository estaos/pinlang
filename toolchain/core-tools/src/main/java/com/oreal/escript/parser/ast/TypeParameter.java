package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/// Defines a type parameter, which are bound able generic types in a type.
///
/// Bounds are constraints that are applied to a generic type that
/// force it to be of a given interface. For example:
///
/// ```
/// function isGreaterThan<T implements Comparable>(T a, T b): boolean {
///     return a.compareTo(b) > 0;
/// }
/// ```
///
/// This shows that type T, a generic type, is bound to be a `Comparable`.
///
/// Multiple bounds can be specified with the `&` character. For example:
///
/// ```
/// T implements Comparable & Serializable
/// ```
///
@Getter
@Setter
@AllArgsConstructor
public class TypeParameter {
    private String name;
    private List<TypeReference> bounds;
}
