package com.oreal.escript.parser;

import com.oreal.escript.extern.Stdio;
import com.oreal.escript.extern.Stdlib;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;

import java.io.FileNotFoundException;
import java.io.IOException;

public class ExternalImportResolver {
    public static CompilationUnit resolve(Import externalImport) throws IOException {
        String fileName = externalImport.getFile().getName();
        return switch(fileName) {
            case "stdio.h" -> Stdio.getCompilationUnit();
            case "stdlib.h" -> Stdlib.getCompilationUnit();
            default -> throw new FileNotFoundException(String.format("Cannot find external import %s", fileName));
        };
    }
}
