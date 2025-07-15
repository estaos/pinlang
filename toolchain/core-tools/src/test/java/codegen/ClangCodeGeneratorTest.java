package codegen;

import codegen.test_utils.CodeGeneratorTestUtils;
import com.oreal.escript.codegen.ClangCodeGenerator;
import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.semantics.Scope;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClangCodeGeneratorTest {
    @Test
    public void writes_files_to_relative_paths() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.get2ScriptCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);

        assertArrayEquals(
                new Path[]{Path.of("main.h"), Path.of("main.c"), Path.of("abc/script2.h"), Path.of("abc/script2.c")},
                files.stream().map(File::getRelativePath).toArray());
    }

    @Test
    public void writes_includes_to_file() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.get2ScriptCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);
        File mainHeaderFile = files.get(0);

        String expectedContents = """
#ifndef MAIN_H_
#define MAIN_H_
#include "abc/script2.h"
#endif // MAIN_H_
""";

        assertEquals(expectedContents, mainHeaderFile.getContents());
    }

    @Test
    public void test_includes_self_header() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.get2ScriptCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);
        File mainHeaderFile = files.get(1);

        String expectedContents = """
#include "main.h"
""";

        assertEquals(expectedContents, mainHeaderFile.getContents());
    }

    @Test
    public void test_can_define_built_in_types() {
        var instance = new ClangCodeGenerator();
        var scope = Scope.getProjectScope();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getBuiltInTypesCompilationUnit(scope);

        List<File> files = instance.generateCode(compilationUnit);
        File mainHeaderFile = files.getFirst();

        String expectedContents = """
#ifndef MAIN_H_
#define MAIN_H_
int8 myInt8;
int16 myInt16;
int32 myInt32;
int64 myInt64;
int128 myInt128;
int256 myInt256;
int512 myInt512;
float myFloat;
double myDouble;
char myChar;
bool myBoolean;
char[] myCharArray;
#endif // MAIN_H_
""";

        assertEquals(expectedContents, mainHeaderFile.getContents());
    }
}
