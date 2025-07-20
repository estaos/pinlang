package com.oreal.escript.codegen;

import com.oreal.escript.codegen.outputs.File;
import com.oreal.escript.parser.ast.AddExpression;
import com.oreal.escript.parser.ast.Argument;
import com.oreal.escript.parser.ast.AssignmentExpression;
import com.oreal.escript.parser.ast.BitwiseAndExpression;
import com.oreal.escript.parser.ast.BitwiseLeftShift;
import com.oreal.escript.parser.ast.BitwiseNotExpression;
import com.oreal.escript.parser.ast.BitwiseOrExpression;
import com.oreal.escript.parser.ast.BitwiseRightShift;
import com.oreal.escript.parser.ast.BitwiseXorExpression;
import com.oreal.escript.parser.ast.BlockExpression;
import com.oreal.escript.parser.ast.BooleanLiteralExpression;
import com.oreal.escript.parser.ast.BreakStatement;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CharLiteralExpression;
import com.oreal.escript.parser.ast.CharSequenceLiteralExpression;
import com.oreal.escript.parser.ast.CompareEqualToExpression;
import com.oreal.escript.parser.ast.CompareGreaterThanExpression;
import com.oreal.escript.parser.ast.CompareLessThanExpression;
import com.oreal.escript.parser.ast.CompareNotEqualToExpression;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.ContinueStatement;
import com.oreal.escript.parser.ast.DecrementExpression;
import com.oreal.escript.parser.ast.DivisionExpression;
import com.oreal.escript.parser.ast.DoWhileLoop;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.ForLoop;
import com.oreal.escript.parser.ast.FunctionCallExpression;
import com.oreal.escript.parser.ast.IfStatement;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.IncrementExpression;
import com.oreal.escript.parser.ast.LogicalAndExpression;
import com.oreal.escript.parser.ast.LogicalNotExpression;
import com.oreal.escript.parser.ast.LogicalOrExpression;
import com.oreal.escript.parser.ast.ModulusExpression;
import com.oreal.escript.parser.ast.MultiplyExpression;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NullExpression;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.ReturnStatement;
import com.oreal.escript.parser.ast.SubtractExpression;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.SymbolValueExpression;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeNameExpression;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.parser.ast.WhileLoop;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.parser.logging.LogEntryCode;

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
    public final List<LogEntry> logs = new LinkedList<>();

    @Override
    public List<File> generateCode(CompilationUnit annotatedCompilationUnit) {
        Deque<Import> importQueue = new LinkedList<>();
        importQueue.add(new Import(null, false, null, annotatedCompilationUnit.getSource(), annotatedCompilationUnit));

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
        return imports.stream().map(importItem -> importItem.getFile().toPath().toString())
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
        } else if(expression instanceof ReturnStatement returnStatement) {
            return String.format("return (%s)", getCExpression(returnStatement.getReturnExpression()));
        } else if(expression instanceof ContinueStatement) {
            return "continue";
        } else if(expression instanceof BreakStatement) {
            return "break";
        } else if(expression instanceof BooleanLiteralExpression booleanLiteralExpression) {
            return Boolean.toString(booleanLiteralExpression.isValue());
        } else if(expression instanceof CharSequenceLiteralExpression charSequenceLiteralExpression) {
            return String.format("\"%s\"", charSequenceLiteralExpression.getCharSequence());
        } else if(expression instanceof CharLiteralExpression charLiteralExpression) {
            return String.format("'%s'", charLiteralExpression.getCharacter());
        } else if(expression instanceof NumberLiteralExpression numberLiteralExpression) {
            return numberLiteralExpression.getNumberAsString();
        } else if(expression instanceof NullExpression) {
            return "NULL";
        }else if(expression instanceof SymbolValueExpression symbolValueExpression) {
            return symbolValueExpression.getSymbolName();
        } else if(expression instanceof AssignmentExpression assignmentExpression) {
            return String.format("%s=(%s)", assignmentExpression.getSymbolName(), getCExpression(assignmentExpression.getValue()));
        } else if(expression instanceof BitwiseAndExpression operator) {
            return String.format("(%s) & (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof BitwiseLeftShift operator) {
            return String.format("(%s) << (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof BitwiseRightShift operator) {
            return String.format("(%s) >> (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof BitwiseNotExpression operator) {
            return String.format("~(%s)", getCExpression(operator.getOperand()));
        } else if(expression instanceof BitwiseOrExpression operator) {
            return String.format("(%s) | (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof BitwiseXorExpression operator) {
            return String.format("(%s) ^ (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof LogicalAndExpression operator) {
            return String.format("(%s) && (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof LogicalOrExpression operator) {
            return String.format("(%s) || (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof LogicalNotExpression operator) {
            return String.format("!(%s)", getCExpression(operator.getOperand()));
        } else if(expression instanceof CompareEqualToExpression operator) {
            return String.format("(%s) == (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof CompareGreaterThanExpression operator) {
            return String.format("(%s) > (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof CompareLessThanExpression operator) {
            return String.format("(%s) < (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof CompareNotEqualToExpression operator) {
            return String.format("(%s) != (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof AddExpression operator) {
            return String.format("(%s) + (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof SubtractExpression operator) {
            return String.format("(%s) - (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof MultiplyExpression operator) {
            return String.format("(%s) * (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof DivisionExpression operator) {
            return String.format("(%s) / (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof ModulusExpression operator) {
            return String.format("(%s) %% (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof DecrementExpression operator) {
            if(operator.isPreDecrement()) {
                return String.format("--(%s)", getCExpression(operator.getOperand()));
            } else {
                return String.format("(%s)++", getCExpression(operator.getOperand()));
            }
        } else if(expression instanceof IncrementExpression operator) {
            if(operator.isPreIncrement()) {
                return String.format("--(%s)", getCExpression(operator.getOperand()));
            } else {
                return String.format("(%s)++", getCExpression(operator.getOperand()));
            }
        } else if(expression instanceof BlockExpression blockExpression) {
            String output = blockExpression.getStatements().stream().map(this::getCExpression).collect(Collectors.joining(";\n"));

            if(output.isEmpty()) {
                return String.format("{%s}", output);
            } else {
                return String.format("{%s}\n", output);
            }
        } else if (expression instanceof FunctionCallExpression functionCallExpression) {
            String arguments = functionCallExpression.getArguments().stream().map(Argument::getExpression)
                    .map(this::getCExpression)
                    .map(cExpression -> String.format("(%s)", cExpression))
                    .collect(Collectors.joining(", "));

            return String.format("%s(%s)", functionCallExpression.getFunctionName(), arguments);
        } else if(expression instanceof DoWhileLoop doWhileLoop) {
            String statements = getCExpression(doWhileLoop.getBlockExpression());
            String booleanCheck = getCExpression(doWhileLoop.getBooleanExpression());
            return String.format("do %s while(%s);", statements, booleanCheck);
        } else if(expression instanceof ForLoop forLoop) {
            String initialisationExpression = getCExpression(forLoop.getDeclarationExpression());
            String comparisonExpression = getCExpression(forLoop.getComparisonExpression());
            String counterExpression = getCExpression(forLoop.getCounterExpression());
            String statements = getCExpression(forLoop.getBlockExpression());

            return String.format("for (%s; %s; %s)\n%s", initialisationExpression, comparisonExpression,
                    counterExpression, statements);
        } else if(expression instanceof IfStatement ifStatement) {
            String booleanCheck = getCExpression(ifStatement.getBooleanExpression());
            String statements = getCExpression(ifStatement.getBlockExpression());
            String elseStatements = Optional.ofNullable(ifStatement.getElseBlockExpression())
                    .map(this::getCExpression)
                    .orElse("");

             StringBuilder output = new StringBuilder(String.format("if(%s) \n%s", booleanCheck, statements));

             for(IfStatement.ElseIfBlock elseIfBlock : ifStatement.getElseIfBlocks()) {
                 String elseIfBooleanCheck = getCExpression(elseIfBlock.getBooleanExpression());
                 String elseIfStatements = getCExpression(elseIfBlock.getBlockExpression());
                 output.append(String.format(" else if(%s) %s", elseIfBooleanCheck, elseIfStatements));
             }

             if(ifStatement.getElseBlockExpression() != null) {
                 output.append(String.format(" else {\n%s}", elseStatements));
             }

             return output.toString();
        } else if(expression instanceof WhileLoop whileLoop) {
            String booleanCheck = getCExpression(whileLoop.getBooleanExpression());
            String statements = getCExpression(whileLoop.getBlockExpression());

            return String.format("while(%s) %s", booleanCheck, statements);
        } else {
            logs.add(LogEntry.warning(expression.getSource(), LogEntryCode.UNKNOWN_EXPRESSION));
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
