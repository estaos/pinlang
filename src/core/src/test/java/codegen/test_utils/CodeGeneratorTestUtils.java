package codegen.test_utils;

import org.estaos.pin.core.parser.ast.BlockExpression;
import org.estaos.pin.core.parser.ast.CallableCode;
import org.estaos.pin.core.parser.ast.CallableType;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.Import;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.Source;
import org.estaos.pin.core.parser.ast.CallableCodeExpression;
import org.estaos.pin.core.parser.ast.TypeReference;
import org.estaos.pin.core.semantics.Scope;

import java.io.File;
import java.util.List;

public class CodeGeneratorTestUtils {
    public static CompilationUnit get2ScriptCompilationUnit() {
        var file = new File("abc/script2.pin");
        var mainFile = new File("main.pin");
        var importedCompilationUnit = new CompilationUnit(file, List.of(), List.of(), List.of());
        var import1 = new Import("", false, new Source(mainFile, 0, 0, 0), file, importedCompilationUnit);

        return  new CompilationUnit(
                mainFile, List.of(import1), List.of(), List.of());
    }

    public static CompilationUnit getCompilationUnitWithExternalStdioImport() {
        var file = new File("stdio.h");
        var mainFile = new File("main.pin");
        var importedCompilationUnit = new CompilationUnit(file, List.of(), List.of(), List.of());
        var import1 = new Import("", true, new Source(mainFile, 0, 0, 0), file, importedCompilationUnit);

        return  new CompilationUnit(
                mainFile, List.of(import1), List.of(), List.of());
    }

    public static CompilationUnit getBuiltInTypesCompilationUnit(Scope scope) {
        var int8TypeReference = new TypeReference("int8", scope.resolveType("int8"), 0, List.of());
        var int16TypeReference = new TypeReference("int16", scope.resolveType("int16"), 0, List.of());
        var int32TypeReference = new TypeReference("int32", scope.resolveType("int32"), 0, List.of());
        var int64TypeReference = new TypeReference("int64", scope.resolveType("int64"), 0, List.of());
        var int128TypeReference = new TypeReference("int128", scope.resolveType("int128"), 0, List.of());
        var int256TypeReference = new TypeReference("int256", scope.resolveType("int256"), 0, List.of());
        var int512TypeReference = new TypeReference("int512", scope.resolveType("int512"), 0, List.of());
        var floatTypeReference = new TypeReference("float", scope.resolveType("float"), 0, List.of());
        var doubleTypeReference = new TypeReference("double", scope.resolveType("double"), 0, List.of());
        var charTypeReference = new TypeReference("char", scope.resolveType("char"), 0, List.of());
        var charArrayTypeReference = new TypeReference("char", scope.resolveType("char"), 1, List.of());
        var booleanTypeReference = new TypeReference("boolean", scope.resolveType("boolean"), 0, List.of());
        var anyTypeReference = new TypeReference("any", scope.resolveType("any"), 0, List.of());

        var source = new Source(new File(""), 0, 0, 0);

        var symbols = List.of(
            new NamedValueSymbol("myInt8", int8TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt16", int16TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt32", int32TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt64", int64TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt128", int128TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt256", int256TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myInt512", int512TypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myFloat", floatTypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myDouble", doubleTypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myChar", charTypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myBoolean", booleanTypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myCharArray", charArrayTypeReference, source, true, false, "", null, false),
            new NamedValueSymbol("myAny", anyTypeReference, source, true, false, "", null, false)
        );

        return  new CompilationUnit(
                new File("main.pin"), List.of(), symbols, List.of());
    }

    public static CompilationUnit getVoidFunctionWithNoStatementsCompilationUnit() {
        var source = new Source(new File(""), 0, 0, 0);

        var callableType = new CallableType(
                source,
                "myFunction_type",
                List.of(), "",
                List.of(), null);

        var typeReference = new TypeReference("myFunction_type", callableType, 0, List.of());
        var callableCode = new CallableCode("myFunction_code", source, callableType, new BlockExpression(null, List.of()));
        var functionSymbol = new NamedValueSymbol("myFunction", typeReference, source, true,
                false, "", new CallableCodeExpression(null, callableCode), false);

        return new CompilationUnit(new File("main.pin"), List.of(), List.of(functionSymbol),
                List.of(callableType), List.of(callableCode));
    }

    public static CompilationUnit getFunctionWithArgsCompilationUnit(Scope scope) {
        var source = new Source(new File(""), 0, 0, 0);
        var int8TypeReference = new TypeReference("int8", scope.resolveType("int8"), 0, List.of());
        var int16TypeReference = new TypeReference("int16", scope.resolveType("int16"), 0, List.of());

        var functionTypeReference = new TypeReference("myFunction_type", null, 0, List.of());
        var callableType = new CallableType(
                source,
                "myFunction_type",
                List.of(), "",
                List.of(
                        new NamedValueSymbol("a", int8TypeReference, source, true, false, "", null, false),
                        new NamedValueSymbol("b", int16TypeReference, source, true, false, "", null, false)
                ),
                functionTypeReference
        );

        functionTypeReference.setType(callableType);
        var callableCode = new CallableCode("myFunction_code", source, callableType, new BlockExpression(null, List.of()));
        var functionSymbol = new NamedValueSymbol("myFunction", functionTypeReference, source, true,
                false, "", new CallableCodeExpression(null, callableCode), false);

        var symbols = List.of(functionSymbol);
        return new CompilationUnit(new File("main.pin"), List.of(), symbols,
                List.of(callableType), List.of(callableCode));
    }
}
