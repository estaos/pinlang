package com.oreal.escript.codegen;

import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeNameExpression;
import com.oreal.escript.parser.ast.TypeReference;

import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
                getTypeForwardDeclarations(compilationUnit.getTypes()) +
                getSymbolDeclarations(compilationUnit.getSymbols(), ";\n", ";\n") +
                getHeaderFileFooter(headerDefinitionName);

        return new File(withFileExtension(".h", path), contents);
    }

    private File generateCFile(CompilationUnit compilationUnit) {
        Path path = compilationUnit.getSource().toPath();
        String contents =
                getCSelfInclude(path) +
                getTypeDefinitions(compilationUnit.getTypes());

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

    private String getSymbolDeclarations(List<? extends Symbol> symbols, String separator, String trailing) {
        String output = symbols.stream().map(this::getSymbolDeclaration).collect(Collectors.joining(separator));

        if(output.isEmpty()) {
            return output;
        } else {
            return output + trailing;
        }
    }

    private String getTypeForwardDeclarations(List<? extends Type> types) {
        return types.stream().map(this::getTypeForwardDeclaration).collect(Collectors.joining());
    }

    private String getTypeDefinitions(List<? extends Type> types) {
        String output = types.stream().map(this::getTypeDefinition).collect(Collectors.joining());

        if(output.isEmpty()) {
            return output;
        } else {
            return output + "\n";
        }
    }

    private String getSymbolDeclaration(Symbol symbol) {
        if(symbol instanceof NamedValueSymbol valueSymbol) {
            if(valueSymbol.isFunction()) {
                CallableType callableType = Objects.requireNonNull((CallableType) valueSymbol.getType().getType());
                if(valueSymbol.getValue() == null) {
                    return String.format("%s_ptr %s", callableType.getName(), valueSymbol.getName());
                } else {
                    return String.format("%s_ptr %s = %s", callableType.getName(), valueSymbol.getName(), getCExpression(valueSymbol.getValue()));
                }
            } else {
                TypeReference type = valueSymbol.getType();
                var cExpression = Optional.ofNullable(valueSymbol.getValue())
                        .map(this::getCExpression);

                String squareBracketPair = IntStream.range(0, valueSymbol.getArrayDimensions())
                        .mapToObj(_ -> "[]")
                        .collect(Collectors.joining());
                String cTypeName =
                        (type.getName().equals("boolean") ? "bool" : type.getName()) + squareBracketPair;

                return cExpression
                        .map(value ->String.format("%s %s = %s", cTypeName, valueSymbol.getName(), value))
                        .orElse(String.format("%s %s", cTypeName, valueSymbol.getName()));
            }
        } else {
            throw new IllegalArgumentException("Unknown symbol " + symbol);
        }
    }

    private String getTypeForwardDeclaration(Type type) {
        if(type instanceof CallableType callableType) {
            String returnType = getCallableTypeReturnType(callableType);
            String parameterList = getCallableTypeParameterList(callableType);

            return String.format("typedef %s (*%s_ptr)(%s);\n%s %s(%s);\n",
                    returnType, callableType.getName(), parameterList,
                    returnType, callableType.getName(), parameterList);
        } else {
            return "";
        }
    }

    private String getTypeDefinition(Type type) {
        if(type instanceof CallableType callableType) {
            String returnType = getCallableTypeReturnType(callableType);
            String parameterList = getCallableTypeParameterList(callableType);

            return String.format("%s %s(%s) {%s}",
                    returnType, callableType.getName(), parameterList,
                    getCExpressions(Objects.requireNonNull(callableType.getStatementBlock()).getStatements(), ";\n", ";\n"));
        } else {
            return "";
        }
    }

    private String getCExpressions(List<? extends Expression> expressions, String separator, String trailing) {
        String output = expressions.stream().map(this::getCExpression).collect(Collectors.joining(separator));

        if(output.isEmpty()) {
            return output;
        } else {
            return output + trailing;
        }
    }


    private String getCExpression(Expression expression) {
        if(expression instanceof TypeNameExpression typeNameExpression) {
            return Objects.requireNonNull(typeNameExpression.getType()).getName();
        } else {
            return "";
        }
    }

    private String getCallableTypeReturnType(CallableType callableType) {
        if(Objects.requireNonNull(callableType.getStatementBlock()).getType() == null) {
            return "void";
        } else if(Objects.requireNonNull(callableType.getStatementBlock()).getType().getType() instanceof CallableType callableReturnType) {
            return String.format("%s_ptr", callableReturnType.getName());
        }else {
            return Objects.requireNonNull(callableType.getStatementBlock()).getType().getName();
        }
    }

    private String getCallableTypeParameterList(CallableType callableType) {
        return getSymbolDeclarations(callableType.getParameters(), ", ", "");
    }
}
