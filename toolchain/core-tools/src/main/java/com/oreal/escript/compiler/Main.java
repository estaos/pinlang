package com.oreal.escript.compiler;

import com.oreal.escript.codegen.ClangCodeGenerator;
import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.DefaultCompilationUnitParser;
import com.oreal.escript.parser.Parser;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.parser.logging.LogEntryType;
import com.oreal.escript.semantics.AstAnnotator;
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
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }

            for (LogEntry entry : parser.getParserLogs()) {
                if(entry.type() == LogEntryType.WARNING) {
                    System.out.println(getLogEntryMessage(entry));
                } else {
                    System.err.println(getLogEntryMessage(entry));
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
