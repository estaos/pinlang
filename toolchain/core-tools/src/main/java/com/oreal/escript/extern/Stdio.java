package com.oreal.escript.extern;

import com.oreal.escript.parser.ast.CompilationUnit;

import java.util.List;

public class Stdio {
    public static CompilationUnit getCompilationUnit() {
        // TODO: Add stdio symbols
        return new CompilationUnit(null, List.of(), List.of(), List.of());
    }
}
