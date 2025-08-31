package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class CompilationUnit {
    private File source;
    private List<Import> imports;

    private List<? extends Symbol> symbols;
    private List<? extends Type> types;
    private List<CallableCode> callableCodeBlocks;

    public CompilationUnit(File source,
                         List<Import> imports,
                         List<? extends Symbol> symbols,
                         List<? extends Type> types) {
        this.source = source;
        this.imports = imports;
        this.symbols = symbols;
        this.types = types;
        this.callableCodeBlocks = List.of();
    }

    public CompilationUnit(File source,
                           List<Import> imports,
                           List<? extends Symbol> symbols,
                           List<? extends Type> types,
                           List<CallableCode> callableCodeBlocks) {
        this.source = source;
        this.imports = imports;
        this.symbols = symbols;
        this.types = types;
        this.callableCodeBlocks = callableCodeBlocks;
    }

    private boolean addedToGlobalScope = false;
}
