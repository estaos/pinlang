package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.Scope;

import java.util.List;

public class RegisterSymbolsAnnotationStep implements AnnotationStep {
    private static final Annotations annotations = new Annotations();

    @Override
    public void annotate(CompilationUnit compilationUnit, Scope globalScope, List<LogEntry> logs) {
        annotations.addSymbolsToScope(compilationUnit, globalScope, logs);
    }
}
