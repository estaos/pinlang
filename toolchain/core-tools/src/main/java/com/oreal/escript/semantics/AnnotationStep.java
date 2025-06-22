package com.oreal.escript.semantics;

import com.oreal.escript.parser.ast.CompilationUnit;

public interface AnnotationStep {
    CompilationUnit annotate(CompilationUnit compilationUnit);
}
