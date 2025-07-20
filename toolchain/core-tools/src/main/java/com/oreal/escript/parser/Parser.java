package com.oreal.escript.parser;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.parser.logging.LogEntryCode;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;


public class Parser {
    private final CompilationUnitParser compilationUnitParser;

    public Parser(CompilationUnitParser compilationUnitParser) {
        this.compilationUnitParser = compilationUnitParser;
    }

    /// Keeps all the compilation units the parser has parsed so that we do not
    /// create multiple compilation units when the file is imported more than once
    /// in the parse tree.
    private final Map<String, CompilationUnit> compilationUnits = new HashMap<>();

    @Getter
    private final List<LogEntry> parserLogs = new LinkedList<>();

    public CompilationUnit parse(File main) {
        final var source = Source.defaultSource(main);
        final var importItem = Import.fromSource(source, main);
        final Queue<Import> imports = new LinkedList<>();
        imports.add(importItem);
        resolveImports(imports);

        return compilationUnits.get(main.getAbsolutePath());
    }

    private void resolveImports(Queue<Import> imports) {
        Import importItem = imports.poll();

        while(importItem != null) {
            final File sourceFile = importItem.getFile();
            final String compilationUnitId = sourceFile.getAbsolutePath();

            if(compilationUnits.containsKey(compilationUnitId)) {
                importItem.setCompilationUnit(compilationUnits.get(compilationUnitId));
            } else {
                try {
                    final @Nullable CompilationUnit compilationUnit = compilationUnitParser.parseImport(importItem, parserLogs);
                    if(compilationUnit != null) {
                        imports.addAll(compilationUnit.getImports());

                        compilationUnits.put(compilationUnitId, compilationUnit);
                        importItem.setCompilationUnit(compilationUnit);
                    }
                } catch(FileNotFoundException exception) {
                    parserLogs.add(LogEntry.error(importItem.getSource(), LogEntryCode.SOURCE_FILE_DOES_NOT_EXIST, exception));
                } catch(IOException exception) {
                    parserLogs.add(LogEntry.error(importItem.getSource(), LogEntryCode.SOURCE_FILE_NOT_READABLE, exception));
                }
            }

            importItem = imports.poll();
        }
    }
}
