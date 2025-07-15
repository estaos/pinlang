package com.oreal.escript.semantics;

import com.oreal.escript.parser.ast.Type;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class Scope {
    private final @Nullable Scope parent;
    private final String name;
    private final List<Type> types = new LinkedList<>();

    /// Represents a scope of symbols that is imported under an alias.
    ///
    /// ```
    /// import "abc.escript"
    /// import "def.escript" as name
    /// ```
    ///
    /// In the above example, contents of abc will be added under this scope, while contents
    /// of def will be under 'name' aliased scope.
    private final Map<String, Scope> aliasedScopes = new HashMap<>();

    public Scope(@Nullable Scope parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    public Scope(String name) {
        this.parent = null;
        this.name = name;
    }

    public Scope registerType(Type type) {
        // TODO: Do not register type if there is already a symbol with same name
        types.add(type);
        return this;
    }

    public Scope registerAliasedScope(String name, Scope scope) {
        // TODO: Do not register if symbol with same name exists
        aliasedScopes.put(name, scope);
        return this;
    }

    public @Nullable Type resolveType(String name) {
        return types.stream().filter(type -> type.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> Optional.ofNullable(parent)
                                .map(parent -> parent.resolveType(name))
                                .orElse(null)
                );

    }

    /// Symbols defined in project scope are accessible from any scope.
    ///
    /// Therefore, all scopes should be supersets of the project scope.
    ///
    /// The project scope has no parent.
    public static Scope getProjectScope() {
        // Note that these are the built in types, i.e, not user defined.
        var int8Type = new Type(null, "int8", List.of(), "");
        var int16Type = new Type(null, "int16", List.of(), "");
        var int32Type = new Type(null, "int32", List.of(), "");
        var int64Type = new Type(null, "int64", List.of(), "");
        var int128Type = new Type(null, "int128", List.of(), "");
        var int256Type = new Type(null, "int256", List.of(), "");
        var int512Type = new Type(null, "int512", List.of(), "");
        var floatType = new Type(null, "float", List.of(), "");
        var doubleType = new Type(null, "double", List.of(), "");

        var charType = new Type(null, "char", List.of(), "");
        var booleanType = new Type(null, "boolean", List.of(), "");

        return new Scope("")
                .registerType(int8Type)
                .registerType(int16Type)
                .registerType(int32Type)
                .registerType(int64Type)
                .registerType(int128Type)
                .registerType(int256Type)
                .registerType(int512Type)
                .registerType(floatType)
                .registerType(doubleType)
                .registerType(charType)
                .registerType(booleanType);
    }
}
