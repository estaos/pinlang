package org.estaos.pin.core.parser;

import org.estaos.pin.core.extern.Assert;
import org.estaos.pin.core.extern.Stdio;
import org.estaos.pin.core.extern.Stdlib;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.Import;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ExternalImportResolver {
    public static CompilationUnit resolve(Import externalImport) throws IOException {
        String fileName = externalImport.getFile().getName();
        return switch(fileName) {
            case "stdio.h" -> Stdio.getCompilationUnit();
            case "stdlib.h" -> Stdlib.getCompilationUnit();
            case "assert.h" -> Assert.getCompilationUnit();
            default -> throw new FileNotFoundException(String.format("Cannot find external import %s", fileName));
        };
    }
}
