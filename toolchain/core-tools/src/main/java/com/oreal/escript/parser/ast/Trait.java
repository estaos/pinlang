package com.oreal.escript.parser.ast;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Trait extends Type {
    public Trait(
            Source source,
            String name,
            List<TypeParameter> typeParameters,
            String documentationMarkdown,
            List<CallableType> methodDeclarations) {
        super(source, name, typeParameters, documentationMarkdown);
        this.methodDeclarations = methodDeclarations;
    }

    private List<CallableType> methodDeclarations;
}
