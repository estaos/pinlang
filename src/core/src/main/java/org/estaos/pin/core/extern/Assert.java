package org.estaos.pin.core.extern;

import org.estaos.pin.core.parser.ast.CallableType;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.Source;

import java.io.File;
import java.util.List;

import static org.estaos.pin.core.parser.ASTBuilderVisitor.USER_DEFINED_TYPE_SIGIL;

public class Assert {
    private static final File file = new File("assert.h");

    public static CompilationUnit getCompilationUnit() {
        NativeSymbols nativeSymbols = getNativeSymbols();
        return new CompilationUnit(file, List.of(), nativeSymbols.variables(), nativeSymbols.types());
    }

    private static NativeSymbols getNativeSymbols() {
        CallableType assertCallableType = getAssertCallableType();
        NamedValueSymbol assertCallable = getNamedValueSymbol("assert", assertCallableType, 0);

        return new NativeSymbols(List.of(assertCallableType), List.of(assertCallable));
    }

    private static CallableType getAssertCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("assert_%s", USER_DEFINED_TYPE_SIGIL),
                List.of(),
                "",
                List.of(),
                null,
                true
        );
    }

    private static NamedValueSymbol getNamedValueSymbol(String name, String typeName, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, typeName, arrayDimensions);
    }

    private static NamedValueSymbol getNamedValueSymbol(String name, CallableType type, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, type, arrayDimensions);
    }
}
