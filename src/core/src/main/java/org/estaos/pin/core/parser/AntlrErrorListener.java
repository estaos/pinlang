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
        List<String> stack = ((Parser) recognizer).getRuleInvocationStack();
        Collections.reverse(stack);
        String ruleStack = "rule stack: " + stack;
        String error = "line "+line+":"+charPositionInLine+" at "+offendingSymbol+": "+msg;

        Source source = new Source(fileBeingParsed, line, charPositionInLine, charPositionInLine+1);
        destination.add(LogEntry.warning(source, LogEntryCode.SYNTAX_LOG, ruleStack, e));
        destination.add(LogEntry.error(source, LogEntryCode.SYNTAX_LOG, error, e));
    }
}
