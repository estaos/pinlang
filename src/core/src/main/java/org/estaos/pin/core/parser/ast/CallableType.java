package org.estaos.pin.core.parser.ast;

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

    public CallableType(
            Source source,
            String identifier,
            List<TypeParameter> typeParameters,
            String documentationMarkdown,
            List<? extends Symbol> parameters,
            @Nullable TypeReference returnType,
            boolean isVarArgs) {
        super(source, identifier, typeParameters, documentationMarkdown, List.of());
        this.parameters = parameters;
        this.returnType = returnType;
        this.isVarArgs = isVarArgs;
    }

    /// These are the parameters it should be called with.
    ///
    /// It is the number and type of parameters that differentiate callable types
    /// of the same name. This allows for method/function overloading.
    private List<? extends Symbol> parameters;

    @Nullable TypeReference returnType;

    private boolean isVarArgs = false;

    @Override
    public boolean isSubTypeOf(Type other) {
        if(other instanceof CallableType otherCallableType) {
            return getName().equals(other.getName())
                    || (
                    returnValuesCheck(otherCallableType)
                    && parametersCheck(otherCallableType.parameters)
            );
        } else {
            return false;
        }

    }

    /// Reject if any of this params is not other params and is subtype of other params
    private boolean parametersCheck(List<? extends Symbol> otherParameters) {
        if(parameters.size() == otherParameters.size()) {
            for(int index = 0; index < parameters.size(); index++) {
                Type thisType = Objects.requireNonNull(parameters.get(index).getType().getType());
                Type otherType = Objects.requireNonNull(otherParameters.get(index).getType().getType());
                if(!thisType.getName().equals(otherType.getName()) && thisType.isSubTypeOf(otherType)) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    /// For return types allow if both are null or this return type is subtype
    /// of other return type.
    private boolean returnValuesCheck(CallableType other) {
        return (returnType == null && other.returnType == null) || (
                    (returnType != null && other.returnType != null)
                            && Objects.requireNonNull(returnType.getType())
                            .isSubTypeOf(Objects.requireNonNull(other.getReturnType().getType()))
                );
    }
}
