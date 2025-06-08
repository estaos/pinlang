package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CompilationUnit {
    private File source;
    private List<Import> imports;

    private List<Symbol> symbols;
    private List<Type> types;
}
