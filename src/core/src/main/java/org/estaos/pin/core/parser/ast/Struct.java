package org.estaos.pin.core.parser.ast;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Struct extends Type {
    public Struct(
            Source source,
            String name,
            List<TypeParameter> typeParameters,
            String documentationMarkdown) {
        super(source, name, typeParameters, documentationMarkdown, List.of());
    }

    private List<NamedValueSymbol> symbols;
    private TypeReference extendsType;
}
