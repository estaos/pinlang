package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Setter
public class CallableType extends Type {
    public CallableType(
            Source source,
            String identifier,
            List<TypeParameter> typeParameters,
            String documentationMarkdown,
            @Nullable BlockExpression statementBlock,
            List<Symbol> parameters) {
        super(source, identifier, typeParameters, documentationMarkdown);
        this.statementBlock = statementBlock;
        this.parameters = parameters;
    }

    /// This is its block of statements;
    ///
    /// Set to `null` if this is a function declaration, like in traits.
    private @Nullable BlockExpression statementBlock;

    /// These are the parameters it should be called with.
    ///
    /// It is the number and type of parameters that differentiate callable types
    /// of the same name. This allows for method/function overloading.
    private List<Symbol> parameters;
}
