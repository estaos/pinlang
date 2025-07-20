package com.oreal.escript.parser.logging;

import com.oreal.escript.parser.ast.Source;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record LogEntry (LogEntryType type, Source source, LogEntryCode code, @Nullable String message, @Nullable Exception exception) {
    public static LogEntry error(Source source, LogEntryCode logEntryCode) {
        return new LogEntry(LogEntryType.ERROR, source, logEntryCode, null, null);
    }

    public static LogEntry error(Source source, LogEntryCode logEntryCode, Exception exception) {
        return new LogEntry(LogEntryType.ERROR, source, logEntryCode, null, exception);
    }

    public static LogEntry error(Source source, LogEntryCode logEntryCode, String message, Exception exception) {
        return new LogEntry(LogEntryType.ERROR, source, logEntryCode, message, exception);
    }

    public static LogEntry warning(Source source, LogEntryCode logEntryCode) {
        return new LogEntry(LogEntryType.WARNING, source, logEntryCode, null, null);
    }
    
    public static LogEntry warning(Source source, LogEntryCode logEntryCode, Exception exception) {
        return new LogEntry(LogEntryType.WARNING, source, logEntryCode, null, exception);
    }

    public static LogEntry warning(Source source, LogEntryCode logEntryCode, String message, Exception exception) {
        return new LogEntry(LogEntryType.WARNING, source, logEntryCode, message, exception);
    }

    public static boolean containsError(List<LogEntry> entries) {
        return !entries.stream().filter(entry -> entry.type() == LogEntryType.ERROR).toList().isEmpty();
    }
}
