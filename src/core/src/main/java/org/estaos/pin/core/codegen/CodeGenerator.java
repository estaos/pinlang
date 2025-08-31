package org.estaos.pin.core.codegen;

import org.estaos.pin.core.codegen.outputs.File;
import org.estaos.pin.core.parser.ast.CompilationUnit;

import java.util.List;

public interface CodeGenerator {
    List<File> generateCode(CompilationUnit annotatedCompilationUnit);
}
