package org.estaos.pin.core.semantics;

import org.estaos.pin.core.parser.ast.Symbol;
import org.estaos.pin.core.parser.ast.Type;
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
    private final List<Symbol> symbols = new LinkedList<>();
    private final boolean isForProject;

    /// Represents a scope of symbols that is imported under an alias.
    ///
    /// ```
    /// import "abc.pin"
    /// import "def.pin" as name
    /// ```
    ///
    /// In the above example, contents of abc will be added under this scope, while contents
    /// of def will be under 'name' aliased scope.
    private final Map<String, Scope> aliasedScopes = new HashMap<>();

    public Scope(@Nullable Scope parent, String name, boolean isProjectScope) {
        this.parent = parent;
        this.name = name;
        this.isForProject = isProjectScope;
    }

    public Scope(String name, boolean isProjectScope) {
        this.parent = null;
        this.name = name;
        this.isForProject = isProjectScope;
    }

    public Scope registerType(Type type) throws IllegalStateException {
        if(!nameAlreadyInScope(type.getName())) {
            types.add(type);
        } else {
            throw new IllegalStateException("Type already present in scope");
        }

        return this;
    }

    public Scope registerSymbol(Symbol symbol) throws IllegalStateException {
        if(!nameAlreadyInScope(symbol.getName())) {
            symbols.add(symbol);
        } else {
            throw new IllegalStateException("Symbol already present in scope");
        }

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

    public @Nullable Symbol resolveSymbol(String name) {
        return symbols.stream().filter(symbol -> symbol.getName().equals(name))
                .findFirst()
                .orElseGet(
                        () -> Optional.ofNullable(parent)
                                .map(parent -> parent.resolveSymbol(name))
                                .orElse(null)
                );

    }

    public boolean nameAlreadyInScope(String name) {
        return resolveType(name) != null || resolveSymbol(name) != null;
    }

    public Scope findProjectScope() {
        return findProjectScope(this);
    }

    private Scope findProjectScope(Scope scope) {
        if(scope.isForProject) {
            return scope;
        } else {
            if(scope.parent == null) {
                // Should never happen as root scope should be project scope in any context.
                throw new IllegalStateException("Scope is not descendant of project scope.");
            } else {
                return findProjectScope(scope.parent);
            }
        }
    }

    /// Symbols defined in project scope are accessible from any scope.
    ///
    /// Therefore, all scopes should be supersets of the project scope.
    ///
    /// The project scope has no parent.
    public static Scope getProjectScope() {
        // Note that these are the built in types, i.e, not user defined.
        var doubleType = new Type(null, "double", List.of(), "", List.of());
        var floatType = new Type(null, "float", List.of(), "", List.of(doubleType));
        var int512Type = new Type(null, "int512", List.of(), "", List.of());
        var int256Type = new Type(null, "int256", List.of(), "", List.of(int512Type));
        var int128Type = new Type(null, "int128", List.of(), "", List.of(int256Type));
        var int64Type = new Type(null, "int64", List.of(), "", List.of(int128Type, doubleType));
        var int32Type = new Type(null, "int32", List.of(), "", List.of(int64Type, floatType));
        var int16Type = new Type(null, "int16", List.of(), "", List.of(int32Type));
        var int8Type = new Type(null, "int8", List.of(), "", List.of(int16Type));


        var charType = new Type(null, "char", List.of(), "", List.of(int8Type));
        var booleanType = new Type(null, "boolean", List.of(), "", List.of());
        var anyType = new Type(null, "any", List.of(), "", List.of());

        return new Scope("", true)
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
                .registerType(booleanType)
                .registerType(anyType);
    }
}
