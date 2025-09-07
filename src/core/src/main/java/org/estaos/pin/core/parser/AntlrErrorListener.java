package org.estaos.pin.core.parser;

import org.estaos.pin.core.parser.ast.Source;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.parser.logging.LogEntryCode;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.io.File;
import java.util.Collections;
import java.util.List;


@AllArgsConstructor
public class AntlrErrorListener extends BaseErrorListener {
    private List<LogEntry> destination;
    private File fileBeingParsed;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        Source source = new Source(fileBeingParsed, line, -1, -1);
        destination.add(LogEntry.error(source, LogEntryCode.SYNTAX_LOG, msg, e));
    }
}
