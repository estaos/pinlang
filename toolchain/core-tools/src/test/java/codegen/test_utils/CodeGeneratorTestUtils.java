package codegen.test_utils;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.semantics.Scope;

import java.io.File;
import java.util.List;

public class CodeGeneratorTestUtils {
    public static CompilationUnit get2ScriptCompilationUnit() {
        var importedCompilationUnit = new CompilationUnit(new File("abc/script2.escript"), List.of(), List.of(), List.of());
        var import1 = new Import("", false, new Source(new File("abc/script2.escript"), 0, 0, 0), importedCompilationUnit);

        return  new CompilationUnit(
                new File("main.escript"), List.of(import1), List.of(), List.of());
    }

    public static CompilationUnit getBuiltInTypesCompilationUnit(Scope scope) {
        var int8TypeReference = new TypeReference("int8", scope.resolveType("int8"), List.of());
        var int16TypeReference = new TypeReference("int16", scope.resolveType("int16"), List.of());
        var int32TypeReference = new TypeReference("int32", scope.resolveType("int32"), List.of());
        var int64TypeReference = new TypeReference("int64", scope.resolveType("int64"), List.of());
        var int128TypeReference = new TypeReference("int128", scope.resolveType("int128"), List.of());
        var int256TypeReference = new TypeReference("int256", scope.resolveType("int256"), List.of());
        var int512TypeReference = new TypeReference("int512", scope.resolveType("int512"), List.of());
        var floatTypeReference = new TypeReference("float", scope.resolveType("float"), List.of());
        var doubleTypeReference = new TypeReference("double", scope.resolveType("double"), List.of());
        var charTypeReference = new TypeReference("char", scope.resolveType("char"), List.of());
        var booleanTypeReference = new TypeReference("boolean", scope.resolveType("boolean"), List.of());

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
            new NamedValueSymbol("myCharArray", charTypeReference, source, true, false, "", null, false, 1)

        );

        return  new CompilationUnit(
                new File("main.escript"), List.of(), symbols, List.of());
    }
}
