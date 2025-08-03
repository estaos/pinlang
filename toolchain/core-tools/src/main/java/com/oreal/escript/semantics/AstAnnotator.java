package com.oreal.escript.semantics;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.filters.AnnotationStep;
import com.oreal.escript.semantics.filters.GlobalVariableDeclarationAnnotationStep;
import com.oreal.escript.semantics.filters.RegisterSymbolsAnnotationStep;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AstAnnotator {
    private final List<AnnotationStep> annotationSteps;

    public void annotate(CompilationUnit ast, List<LogEntry> logs) {
        Scope scope = Scope.getProjectScope();
        for(AnnotationStep annotationStep: annotationSteps) {
            annotationStep.annotate(ast, scope, logs);
        }
    }

    public static AstAnnotator getDefaultAnnotator() {
        return new AstAnnotator(List.of(
                new RegisterSymbolsAnnotationStep(),
                new GlobalVariableDeclarationAnnotationStep()
        ));
    }
}
