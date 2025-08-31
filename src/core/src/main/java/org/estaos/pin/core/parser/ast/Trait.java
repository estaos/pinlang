package org.estaos.pin.core.parser.ast;

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
        super(source, name, typeParameters, documentationMarkdown, List.of());
        this.methodDeclarations = methodDeclarations;
    }

    private List<CallableType> methodDeclarations;
}
