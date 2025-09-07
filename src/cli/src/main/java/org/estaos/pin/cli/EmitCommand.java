package org.estaos.pin.cli;

import org.estaos.pin.core.codegen.ClangCodeGenerator;
import org.estaos.pin.core.codegen.outputs.File;
import org.estaos.pin.core.parser.DefaultCompilationUnitParser;
import org.estaos.pin.core.parser.Parser;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.parser.logging.LogEntryType;
import org.estaos.pin.core.semantics.AstAnnotator;
import org.fusesource.jansi.AnsiConsole;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;

import static org.fusesource.jansi.Ansi.ansi;

@CommandLine.Command(
        name = "emit",
        description = "Transpile a pin file to c source and header files"
)
public class EmitCommand implements Runnable {
    @CommandLine.Parameters(index = "0", description = "The Pin file to transpile")
    String inputFile;

    @CommandLine.Option(
            names = {"-v", "--verbose"},
            description = "Enable to print more detailed logs"
    )
    boolean verboseLogging = false;

    @Override
    public void run() {
        AnsiConsole.systemInstall();

        var compilationUnitParser = new DefaultCompilationUnitParser();
        var parser = new Parser(compilationUnitParser);
        var astAnnotator = AstAnnotator.getDefaultAnnotator();

        @Nullable CompilationUnit compilationUnit = parser.parse(new java.io.File(inputFile));
        if(compilationUnit != null && !LogEntry.containsError(parser.getParserLogs())) {
            astAnnotator.annotate(compilationUnit, parser.getParserLogs());
        }

        if(!LogEntry.containsError(parser.getParserLogs())) {
            List<File> cFiles = new ClangCodeGenerator().generateCode(compilationUnit);
            saveFiles(cFiles);
        } else {
            printLogs(parser.getParserLogs(), verboseLogging);
            System.exit(1);
        }
    }

    private void saveFiles(List<File> files) {
        try {
            for(File file : files) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getRelativePath().toFile()))) {
                    writer.write(file.getContents());
                }
            }
        } catch(IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

    private void printLogs(List<LogEntry> entries, boolean verbose) {
        int errorCount = 0;
        int warningCount = 0;

        for (LogEntry entry : entries) {
            if(entry.type() == LogEntryType.ERROR) {
                errorCount++;
            } else if(entry.type() == LogEntryType.WARNING) {
                warningCount++;
            }

            printLogEntry(entry, verbose);
        }

        if(errorCount > 0) {
            prettyPrint(String.format(
                    "Compilation failed: @|red %d|@ errors, @|yellow %d|@ warnings",
                    errorCount, warningCount), System.out);
        }
    }

    private void printLogEntry(LogEntry logEntry, boolean verbose) {
        String message = Optional.ofNullable(logEntry.message()).orElse(logEntry.code().toString());
        String source = getSourceAsString(logEntry);

        String logType = switch(logEntry.type()) {
            case ERROR -> "@|red ERROR|@";
            case WARNING -> "@|yellow WARNING|@";
        };

        String rawError;
        if(verbose) {
            rawError = "\n" + logEntry;
        } else {
            rawError = "";
        }

        String printableLog = String.format("%s %s %s%s", source, logType, message, rawError);
        if(logEntry.type() == LogEntryType.ERROR) {
            prettyPrint(printableLog, System.err);
        } else {
            prettyPrint(printableLog, System.out);
        }
    }

    private String getSourceAsString(LogEntry logEntry) {
        String sourceFile = logEntry.source().getFile().getName();

        String source;
        if(logEntry.source().getEndIndex() < 0 || logEntry.source().getStartIndex() < 0) {
            source = String.format("%s:%d", sourceFile, logEntry.source().getLineNumber());
        } else {
            source = String.format("%s:%d:%d-%d", sourceFile,
                    logEntry.source().getLineNumber(),
                    logEntry.source().getStartIndex(),
                    logEntry.source().getEndIndex());
        }

        return source;
    }

    private void prettyPrint(String toPrint, PrintStream printStream) {
        printStream.println(ansi().render(toPrint) );
    }
}
