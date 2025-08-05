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

public class Stdlib {
    private static final File file = new File("stdlib.h");

    public static CompilationUnit getCompilationUnit() {
        NativeSymbols nativeSymbols = getNativeSymbols();
        return new CompilationUnit(file, List.of(), nativeSymbols.variables(), nativeSymbols.types());
    }

    private static NativeSymbols getNativeSymbols() {
        CallableType mallocCallableType = getMallocCallableType();
        NamedValueSymbol mallocCallable = getNamedValueSymbol("malloc", mallocCallableType, 0);

        CallableType callocCallableType = getCallocCallableType();
        NamedValueSymbol callocCallable = getNamedValueSymbol("calloc", callocCallableType, 0);

        CallableType reallocCallableType = getReallocCallableType();
        NamedValueSymbol reallocCallable = getNamedValueSymbol("realloc", reallocCallableType, 0);

        CallableType freeCallableType = getFreeCallableType();
        NamedValueSymbol freeCallable = getNamedValueSymbol("free", freeCallableType, 0);

        CallableType sizeofCallableType = getSizeofCallableType();
        NamedValueSymbol sizeofCallable = getNamedValueSymbol("sizeof", sizeofCallableType, 0);

        return new NativeSymbols(List.of(
                mallocCallableType,
                callocCallableType,
                reallocCallableType,
                freeCallableType,
                sizeofCallableType
        ), List.of(
                mallocCallable,
                callocCallable,
                reallocCallable,
                freeCallable,
                sizeofCallable
        ));
    }

    private static CallableType getMallocCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("malloc_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(getNamedValueSymbol("size", "int64", 0)),
                TypeReference.ofType("any", 0), // void*
                false
        );
    }

    private static CallableType getCallocCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("calloc_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(
                        getNamedValueSymbol("count", "int64", 0),
                        getNamedValueSymbol("size", "int64", 0)
                ),
                TypeReference.ofType("any", 0),
                false
        );
    }

    private static CallableType getReallocCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("realloc_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(
                        getNamedValueSymbol("ptr", "any", 0),
                        getNamedValueSymbol("new_size", "int64", 0)
                ),
                TypeReference.ofType("any", 0),
                false
        );
    }

    private static CallableType getFreeCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("free_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(getNamedValueSymbol("ptr", "any", 0)),
                null, false
        );
    }

    // Adding it so it has a home even though it is not defined in stdlib.
    private static CallableType getSizeofCallableType() {
        return new CallableType(
                Source.defaultSource(file),
                String.format("sizeof_%s", CALLABLE_TYPE_SIGIL),
                List.of(),
                "",
                List.of(), TypeReference.ofType("int64"), true
        );
    }


    private static NamedValueSymbol getNamedValueSymbol(String name, String typeName, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, typeName, arrayDimensions);
    }

    private static NamedValueSymbol getNamedValueSymbol(String name, Type type, int arrayDimensions) {
        return AstUtils.getNamedValueSymbol(file, name, type, arrayDimensions);
    }
}
