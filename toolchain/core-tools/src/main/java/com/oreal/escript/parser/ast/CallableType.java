package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class CallableType extends Type {
    public CallableType(
            Source source,
            String identifier,
            List<TypeParameter> typeParameters,
            String documentationMarkdown,
            List<? extends Symbol> parameters,
            @Nullable TypeReference returnType) {
        super(source, identifier, typeParameters, documentationMarkdown, List.of());
        this.parameters = parameters;
        this.returnType = returnType;
    }

    /// These are the parameters it should be called with.
    ///
    /// It is the number and type of parameters that differentiate callable types
    /// of the same name. This allows for method/function overloading.
    private List<? extends Symbol> parameters;

    @Nullable TypeReference returnType;

    @Override
    public boolean isSubTypeOf(Type other) {
        if(other instanceof CallableType otherCallableType) {
            return getName().equals(other.getName())
                    || (
                    returnTypeIsSubTypeOf(Objects.requireNonNull(otherCallableType.returnType).getType())
                    && parametersAreAreSubTypesOf(otherCallableType.parameters)
            );
        } else {
            return false;
        }

    }

    private boolean returnTypeIsSubTypeOf(Type other) {
        return Objects.requireNonNull(Objects.requireNonNull(returnType).getType()).isSubTypeOf(other);
    }

    private boolean parametersAreAreSubTypesOf(List<? extends Symbol> otherParameters) {
        if(parameters.size() == otherParameters.size()) {
            for(int index = 0; index < parameters.size(); index++) {
                Type thisType = Objects.requireNonNull(parameters.get(index).getType().getType());
                Type otherType = Objects.requireNonNull(otherParameters.get(index).getType().getType());
                if(!thisType.isSubTypeOf(otherType)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }
}
