package com.oreal.escript.codegen;

import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;

import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClangCodeGenerator implements  CodeGenerator {
    private final String ES_EXTENSION = ".escript";

    @Override
    public List<File> generateCode(CompilationUnit annotatedCompilationUnit) {
        Deque<Import> importQueue = new LinkedList<>();
        importQueue.add(new Import(null, false, null, annotatedCompilationUnit));

        List<File> outputs = new LinkedList<>();
        while(!importQueue.isEmpty()) {
            Import importItem = importQueue.poll();
            if(!importItem.isExternal()) {
                importQueue.addAll(Objects.requireNonNull(importItem.getCompilationUnit()).getImports());

                outputs.add(generateHeaderFile(importItem.getCompilationUnit()));
                outputs.add(generateCFile(importItem.getCompilationUnit()));
            }
        }

        return outputs;
    }

    private File generateHeaderFile(CompilationUnit compilationUnit) {
        Path path = compilationUnit.getSource().toPath();
        String headerDefinitionName = getHeaderDefinitionName(path);

        String contents =
                getHeaderFileHeader(headerDefinitionName) +
                getCIncludes(compilationUnit.getImports()) +
                getHeaderFileFooter(headerDefinitionName);

        return new File(withFileExtension(".h", path), contents);
    }

    private File generateCFile(CompilationUnit compilationUnit) {
        Path path = compilationUnit.getSource().toPath();
        String contents = getCSelfInclude(path);

        return new File(withFileExtension(".c", path), contents);
    }

    private Path withFileExtension(String newExtension, Path path) {
        String filename = path.getFileName().toString();
        int extensionStart = filename.lastIndexOf(".");

        String newFilename = filename;
        if(extensionStart != -1) {
            newFilename = filename.substring(0, extensionStart) + newExtension;
        }

        if(path.getParent() == null) {
            return Path.of(newFilename);
        } else {
            return path.getParent().resolve(newFilename);
        }
    }

    private String getHeaderFileHeader(String headerDefinitionName) {
        return String.format("#ifndef %s\n#define %s\n", headerDefinitionName, headerDefinitionName);
    }

    private String getCIncludes(List<Import> imports) {
        return imports.stream().map(importItem -> importItem.getSource().getFile().toPath().toString())
                .map(path -> String.format("#include \"%s\"\n", path.replace("\\", "/")
                        .replace(ES_EXTENSION, ".h")))
                .collect(Collectors.joining());
    }

    private String getCSelfInclude(Path path) {
        String includePath = withFileExtension(".h", path).getFileName().toString();
        return String.format("#include \"%s\"\n", includePath);
    }

    private String getHeaderFileFooter(String headerDefinitionName) {
        return String.format("#endif // %s\n", headerDefinitionName);
    }

    private String getHeaderDefinitionName(Path path) {
        return path.toString()
                .replace(path.getFileSystem().getSeparator(), "_")
                .replace(ES_EXTENSION, "_H_")
                .replace(".", "_").toUpperCase();
    }
}
