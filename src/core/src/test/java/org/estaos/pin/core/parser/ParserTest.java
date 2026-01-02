package org.estaos.pin.core.parser;

import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ParserTest {
    @Test
    public void should_return_main_compilation_unit() throws IOException {
        final var mockCompilationUnitParser = mock(CompilationUnitParser.class);
        when(mockCompilationUnitParser.parseImport(any(), any())).thenReturn(new CompilationUnit(
                new File("main.pin"), List.of(), List.of(), List.of()));

        final var instance = new Parser(mockCompilationUnitParser);
        final CompilationUnit returnValue = instance.parse(new File("main.pin"));

        assertEquals(new File("main.pin"), returnValue.getSource());
    }
}
