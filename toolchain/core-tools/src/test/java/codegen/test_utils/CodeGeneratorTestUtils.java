package codegen.test_utils;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.Source;

import java.io.File;
import java.util.List;

public class CodeGeneratorTestUtils {
    public static CompilationUnit get2ScriptCompilationUnit() {
        var importedCompilationUnit = new CompilationUnit(new File("abc/script2.escript"), List.of(), List.of(), List.of());
        var import1 = new Import("", false, new Source(new File("abc/script2.escript"), 0, 0, 0), importedCompilationUnit);

        return  new CompilationUnit(
                new File("main.escript"), List.of(import1), List.of(), List.of());
    }
}
