package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
@AllArgsConstructor
public class CompilationUnit {
    private final File source;
    private final List<Import> imports;

    private final List<Symbol> symbols;
}
