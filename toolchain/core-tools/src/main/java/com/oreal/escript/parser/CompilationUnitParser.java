package com.oreal.escript.parser;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.logging.LogEntry;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.List;

/// Parses a compilation unit from source file.
///
/// The resulting compilation unit will have its imports not
/// yet resolved, i.e. the imports will have null compilation units.
public interface CompilationUnitParser {
    @Nullable CompilationUnit parseImport(Import importItem, List<LogEntry> parserLogs) throws IOException;
}
