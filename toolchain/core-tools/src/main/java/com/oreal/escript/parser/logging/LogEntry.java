package com.oreal.escript.parser.logging;

import com.oreal.escript.parser.ast.Source;
import org.jetbrains.annotations.Nullable;

public record LogEntry (LogEntryType type, Source source, LogEntryCode code, @Nullable Exception exception) {
    public static LogEntry error(Source source, LogEntryCode logEntryCode) {
        return new LogEntry(LogEntryType.ERROR, source, logEntryCode, null);
    }

    public static LogEntry error(Source source, LogEntryCode logEntryCode, Exception exception) {
        return new LogEntry(LogEntryType.ERROR, source, logEntryCode, exception);
    }

    public static LogEntry warning(Source source, LogEntryCode logEntryCode) {
        return new LogEntry(LogEntryType.WARNING, source, logEntryCode, null);
    }
    
    public static LogEntry warning(Source source, LogEntryCode logEntryCode, Exception exception) {
        return new LogEntry(LogEntryType.WARNING, source, logEntryCode, exception);
    }
}
