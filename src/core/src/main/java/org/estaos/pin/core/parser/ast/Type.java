package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public abstract class Type {
    /// Where this type is defined. Is null for built-in types.
    private @Nullable Source source;
    private String name;
    private List<TypeParameter> typeParameters;
    private String documentationMarkdown;

    // TODO: Remove as is not needed in sound types
    private List<Type> superTypes;

    // TODO: Remove as is not needed for sound types
    public boolean isSubTypeOf(Type other) {
        return name.equals(other.name)
                || superTypes.stream().anyMatch(superType -> superType.name.equals(other.name)
                || superType.isSubTypeOf(other));
    }

    public abstract boolean canBeCastedTo(Type other);
    public abstract boolean canBeAssigned(Type other);
}
