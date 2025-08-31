package org.estaos.pin.core.semantics.filters;

import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.semantics.Scope;

import java.util.List;

public class RegisterSymbolsAnnotationStep implements AnnotationStep {
    private static final Annotations annotations = new Annotations();

    @Override
    public void annotate(CompilationUnit compilationUnit, Scope globalScope, List<LogEntry> logs) {
        annotations.addSymbolsToScope(compilationUnit, globalScope, logs);
    }
}
