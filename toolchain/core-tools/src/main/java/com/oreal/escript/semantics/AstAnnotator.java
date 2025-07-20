package com.oreal.escript.semantics;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.logging.LogEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AstAnnotator {
    private final List<AnnotationStep> annotationSteps;

    public CompilationUnit annotate(CompilationUnit ast, List<LogEntry> logs) {
        return ast;
    }

    public static AstAnnotator getDefaultAnnotator() {
        return new AstAnnotator(List.of());
    }
}
