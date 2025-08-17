package com.oreal.escript.extern;

import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeReference;

import java.io.File;
import java.util.List;

import static com.oreal.escript.parser.ASTBuilderVisitor.CALLABLE_TYPE_SIGIL;

public class Stdio {
    private static final File file = new File("stdio.h");

    public static CompilationUnit getCompilationUnit() {
        NativeSymbols nativeSymbols = getNativeSymbols();
        return new CompilationUnit(file, List.of(), nativeSymbols.variables(), nativeSymbols.types());
    }

    private static NativeSymbols getNativeSymbols() {
        CallableType printFCallableType = getPrintFCallableType();
        NamedValueSymbol printFCallable = getNamedValueSymbol("printf", printFCallableType, 0);

        CallableType scanFCallableType = getScanFCallableType();
        NamedValueSymbol scanFCallable = getNamedValueSymbol("scanf", scanFCallableType, 0);

        CallableType scanFSCallableType = getScanFSCallableType();
        NamedValueSymbol scanFSCallable = getNamedValueSymbol("scanf_s", scanFSCallableType, 0);

        return new NativeSymbols(List.of(
                printFCallableType,
                scanFCallableType,
                scanFSCallableType
        ), List.of(
                printFCallable,
                scanFCallable,
                scanFSCallable
        ));
    }

    private static CallableType getPrintFCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("printf_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(getNamedValueSymbol("format", "char", 1)),
                TypeReference.ofType("int64"),
                true
        );
    }

    private static CallableType getScanFCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("scanf_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(getNamedValueSymbol("format", "char", 1)),
                TypeReference.ofType("int64"),
                true
        );
    }

    private static CallableType getScanFSCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("scanf_s_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(getNamedValueSymbol("format", "char", 1)),
                TypeReference.ofType("int64"),
                true
        );
    }

    private static NamedValueSymbol getNamedValueSymbol(String name, String typeName, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, typeName, arrayDimensions);
    }

    private static NamedValueSymbol getNamedValueSymbol(String name, Type type, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, type, arrayDimensions);
    }
}
