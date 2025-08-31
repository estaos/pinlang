package org.estaos.pin.core.semantics;

import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.semantics.filters.AnnotationStep;
import org.estaos.pin.core.semantics.filters.GlobalVariableDeclarationAnnotationStep;
import org.estaos.pin.core.semantics.filters.RegisterSymbolsAnnotationStep;
import org.estaos.pin.core.semantics.filters.AnnotateUserTypesAnnotationStep;
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
                new GlobalVariableDeclarationAnnotationStep(),
                new AnnotateUserTypesAnnotationStep()
        ));
    }
}
