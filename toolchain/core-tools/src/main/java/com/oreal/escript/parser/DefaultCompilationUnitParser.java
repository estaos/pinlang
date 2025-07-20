package com.oreal.escript.parser;

import com.oreal.escript.antlr.EScriptLexer;
import com.oreal.escript.antlr.EScriptParser;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.logging.LogEntry;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DefaultCompilationUnitParser implements CompilationUnitParser {
    @Override
    public @Nullable CompilationUnit parseImport(Import importItem, List<LogEntry> parserLogs) throws IOException {
        if(importItem.isExternal()) {
            return ExternalImportResolver.resolve(importItem);
        } else {
            File importFile = importItem.getFile();

            try (InputStream inputStream = new FileInputStream(importFile)) {
                var antlrInputStream = new ANTLRInputStream(inputStream);
                var lexer = new EScriptLexer(antlrInputStream);
                var tokens = new CommonTokenStream(lexer);
                var parser = new EScriptParser(tokens);

                var errorListener = new AntlrErrorListener(parserLogs, importFile);
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);

                var visitor = new ASTBuilderVisitor(importFile);
                EScriptParser.CompilationUnitContext context = parser.compilationUnit();

                if(LogEntry.containsError(parserLogs)) {
                    return null;
                } else {
                    return visitor.visit(context);
                }
            }
        }
    }
}
