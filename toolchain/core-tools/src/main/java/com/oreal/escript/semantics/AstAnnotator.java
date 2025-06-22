package com.oreal.escript.semantics;

import com.oreal.escript.parser.ast.CompilationUnit;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AstAnnotator {
    private final List<AnnotationStep> annotationSteps;

    public CompilationUnit annotate(CompilationUnit ast) {
        return ast;
    }

    public static AstAnnotator getDefaultAnnotator() {
        return new AstAnnotator(List.of());
    }
}
