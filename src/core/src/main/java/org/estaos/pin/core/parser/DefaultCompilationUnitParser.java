package org.estaos.pin.core.parser;

import org.estaos.pin.antlr.PinLangLexer;
import org.estaos.pin.antlr.PinLangParser;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.Import;
import org.estaos.pin.core.parser.logging.LogEntry;
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
                var lexer = new PinLangLexer(antlrInputStream);
                var tokens = new CommonTokenStream(lexer);
                var parser = new PinLangParser(tokens);

                var errorListener = new AntlrErrorListener(parserLogs, importFile);
                parser.removeErrorListeners();
                parser.addErrorListener(errorListener);

                var visitor = new ASTBuilderVisitor(importFile);
                PinLangParser.CompilationUnitContext context = parser.compilationUnit();

                if(LogEntry.containsError(parserLogs)) {
                    return null;
                } else {
                    return visitor.visit(context);
                }
            }
        }
    }
}
