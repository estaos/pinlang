package codegen;

import codegen.test_utils.CodeGeneratorTestUtils;
import org.estaos.pin.core.codegen.ClangCodeGenerator;
import org.estaos.pin.core.codegen.outputs.ClangRuntime;
import org.estaos.pin.core.codegen.outputs.File;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.semantics.Scope;
import org.junit.jupiter.api.Disabled;
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
    @Disabled("Disabled due to temp fix for broken import paths")
    public void writes_includes_to_file() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.get2ScriptCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);
        File mainHeaderFile = files.get(0);

        String expectedContents = ClangRuntime.RUNTIME + """
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
    public void test_adds_include_for_external_import() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getCompilationUnitWithExternalStdioImport();

        List<File> files = instance.generateCode(compilationUnit);
        File file = files.getFirst();

        String expectedContents = ClangRuntime.RUNTIME + """
#ifndef MAIN_H_
#define MAIN_H_
#include "stdio.h"
#endif // MAIN_H_
""";

        assertEquals(expectedContents, file.getContents());
    }

    @Test
    public void test_can_define_built_in_types() {
        var instance = new ClangCodeGenerator();
        var scope = Scope.getProjectScope();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getBuiltInTypesCompilationUnit(scope);

        List<File> files = instance.generateCode(compilationUnit);
        File file = files.getFirst();
        File mainHeaderFile = files.get(1);

        String expectedHeaderContents = ClangRuntime.RUNTIME + """
#ifndef MAIN_H_
#define MAIN_H_
extern int8 myInt8;
extern int16 myInt16;
extern int32 myInt32;
extern int64 myInt64;
extern int128 myInt128;
extern int256 myInt256;
extern int512 myInt512;
extern float myFloat;
extern double myDouble;
extern char myChar;
extern bool myBoolean;
extern char *myCharArray;
extern void* myAny;
#endif // MAIN_H_
""";

        String expectedMainContents = """
#include "main.h"
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
char *myCharArray;
void* myAny;
""";

        assertEquals(expectedHeaderContents, file.getContents());
        assertEquals(expectedMainContents, mainHeaderFile.getContents());
    }

    @Test
    public void test_can_declare_function_in_header_file() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getVoidFunctionWithNoStatementsCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);
        File file = files.getFirst();

        String expectedContents = ClangRuntime.RUNTIME + """
#ifndef MAIN_H_
#define MAIN_H_
typedef void (*myFunction_type)();
void myFunction_code();
extern myFunction_type myFunction;
#endif // MAIN_H_
""";

        assertEquals(expectedContents, file.getContents());
    }

    @Test
    public void test_can_define_function_in_c_file() {
        var instance = new ClangCodeGenerator();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getVoidFunctionWithNoStatementsCompilationUnit();

        List<File> files = instance.generateCode(compilationUnit);
        File file = files.get(1);

        String expectedContents = """
#include "main.h"
void myFunction_code() {}
myFunction_type myFunction = myFunction_code;
""";

        assertEquals(expectedContents, file.getContents());
    }

    @Test
    public void test_can_declare_non_void_function_in_header_file() {
        var instance = new ClangCodeGenerator();
        var scope = Scope.getProjectScope();
        CompilationUnit compilationUnit = CodeGeneratorTestUtils.getFunctionWithArgsCompilationUnit(scope);

        List<File> files = instance.generateCode(compilationUnit);
        File file = files.getFirst();

        String expectedContents = ClangRuntime.RUNTIME + """
#ifndef MAIN_H_
#define MAIN_H_
typedef myFunction_type (*myFunction_type)(int8 a, int16 b);
myFunction_type myFunction_code(int8 a, int16 b);
extern myFunction_type myFunction;
#endif // MAIN_H_
""";

        assertEquals(expectedContents, file.getContents());
    }
}
