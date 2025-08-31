package org.estaos.pin.transpiler;

import org.estaos.pin.core.codegen.ClangCodeGenerator;
import org.estaos.pin.core.codegen.outputs.File;
import org.estaos.pin.core.parser.DefaultCompilationUnitParser;
import org.estaos.pin.core.parser.Parser;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.parser.logging.LogEntryType;
import org.estaos.pin.core.semantics.AstAnnotator;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if(args.length == 0) {
            System.err.println("No source to compile");
        } else {
            var compilationUnitParser = new DefaultCompilationUnitParser();
            var parser = new Parser(compilationUnitParser);
            var astAnnotator = AstAnnotator.getDefaultAnnotator();

            try {
                @Nullable CompilationUnit compilationUnit = parser.parse(new java.io.File(args[0]));
                if(compilationUnit != null && !LogEntry.containsError(parser.getParserLogs())) {
                    astAnnotator.annotate(compilationUnit, parser.getParserLogs());
                }

                if(!LogEntry.containsError(parser.getParserLogs())) {
                    List<File> cFiles = new ClangCodeGenerator().generateCode(compilationUnit);
                    saveFiles(cFiles);
                } else {
                    throw new RuntimeException("Error compiling files ... see errors above.");
                }
            } finally {
                for (LogEntry entry : parser.getParserLogs()) {
                    if(entry.type() == LogEntryType.WARNING) {
                        System.out.println(getLogEntryMessage(entry));
                    } else {
                        System.err.println(getLogEntryMessage(entry));
                    }
                }
            }
        }
    }

    private static void saveFiles(List<File> files) throws IOException {
        for(File file : files) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getRelativePath().toFile()))) {
                writer.write(file.getContents());
            }
        }
    }

    private static String getLogEntryMessage(LogEntry entry) {
        return entry.toString();
    }
}
