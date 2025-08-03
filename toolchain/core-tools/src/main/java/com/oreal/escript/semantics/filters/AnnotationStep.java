package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.Scope;

import java.util.List;

public interface AnnotationStep {
    void annotate(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs);
}
