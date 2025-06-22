package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class Type {
    private Source source;
    private String name;
    private List<TypeParameter> typeParameters;
    private String documentationMarkdown;
}
