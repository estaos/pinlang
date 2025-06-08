package com.oreal.escript.codegen;

import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.ast.CompilationUnit;

import java.util.List;

public interface CodeGenerator {
    List<File> generateCode(CompilationUnit annotatedCompilationUnit);
}
