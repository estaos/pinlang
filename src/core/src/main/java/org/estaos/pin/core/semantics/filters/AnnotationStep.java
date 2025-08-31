package org.estaos.pin.core.semantics.filters;

import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.semantics.Scope;

import java.util.List;

public interface AnnotationStep {
    void annotate(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs);
}
