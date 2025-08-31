package org.estaos.pin.core.codegen;

import org.estaos.pin.core.codegen.outputs.ClangRuntime;
import org.estaos.pin.core.codegen.outputs.File;
import org.estaos.pin.core.parser.ast.AddExpression;
import org.estaos.pin.core.parser.ast.Argument;
import org.estaos.pin.core.parser.ast.AssignmentExpression;
import org.estaos.pin.core.parser.ast.BitwiseAndExpression;
import org.estaos.pin.core.parser.ast.BitwiseLeftShift;
import org.estaos.pin.core.parser.ast.BitwiseNotExpression;
import org.estaos.pin.core.parser.ast.BitwiseOrExpression;
import org.estaos.pin.core.parser.ast.BitwiseRightShift;
import org.estaos.pin.core.parser.ast.BitwiseXorExpression;
import org.estaos.pin.core.parser.ast.BlockExpression;
import org.estaos.pin.core.parser.ast.BooleanLiteralExpression;
import org.estaos.pin.core.parser.ast.BreakStatement;
import org.estaos.pin.core.parser.ast.CallableCode;
import org.estaos.pin.core.parser.ast.CallableType;
import org.estaos.pin.core.parser.ast.CharLiteralExpression;
import org.estaos.pin.core.parser.ast.CharSequenceLiteralExpression;
import org.estaos.pin.core.parser.ast.CompareEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareGreaterThanEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareGreaterThanExpression;
import org.estaos.pin.core.parser.ast.CompareLessThanEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareLessThanExpression;
import org.estaos.pin.core.parser.ast.CompareNotEqualToExpression;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.ContinueStatement;
import org.estaos.pin.core.parser.ast.DecrementExpression;
import org.estaos.pin.core.parser.ast.DivisionExpression;
import org.estaos.pin.core.parser.ast.DoWhileLoop;
import org.estaos.pin.core.parser.ast.ExplicitCastExpression;
import org.estaos.pin.core.parser.ast.Expression;
import org.estaos.pin.core.parser.ast.ForLoop;
import org.estaos.pin.core.parser.ast.FunctionCallExpression;
import org.estaos.pin.core.parser.ast.IfStatement;
import org.estaos.pin.core.parser.ast.Import;
import org.estaos.pin.core.parser.ast.IncrementExpression;
import org.estaos.pin.core.parser.ast.LogicalAndExpression;
import org.estaos.pin.core.parser.ast.LogicalNotExpression;
import org.estaos.pin.core.parser.ast.LogicalOrExpression;
import org.estaos.pin.core.parser.ast.ModulusExpression;
import org.estaos.pin.core.parser.ast.MultiplyExpression;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.NullExpression;
import org.estaos.pin.core.parser.ast.NumberLiteralExpression;
import org.estaos.pin.core.parser.ast.ReturnStatement;
import org.estaos.pin.core.parser.ast.SubtractExpression;
import org.estaos.pin.core.parser.ast.Symbol;
import org.estaos.pin.core.parser.ast.SymbolValueExpression;
import org.estaos.pin.core.parser.ast.Type;
import org.estaos.pin.core.parser.ast.CallableCodeExpression;
import org.estaos.pin.core.parser.ast.TypePassExpression;
import org.estaos.pin.core.parser.ast.TypeReference;
import org.estaos.pin.core.parser.ast.VariableDeclaration;
import org.estaos.pin.core.parser.ast.WhileLoop;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.parser.logging.LogEntryCode;

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
                ClangRuntime.RUNTIME +
                getHeaderFileHeader(headerDefinitionName) +
                getCIncludes(compilationUnit.getImports()) +
                getTypeForwardDeclarations(compilationUnit.getTypes(), compilationUnit.getCallableCodeBlocks()) +
                getExternSymbolDeclarations(compilationUnit.getSymbols(), ";\n", ";\n") +
                getHeaderFileFooter(headerDefinitionName);

        return new File(withFileExtension(".h", path), contents);
    }

    private File generateCFile(CompilationUnit compilationUnit) {
        Path path = compilationUnit.getSource().toPath();
        String contents =
                getCSelfInclude(path) +
                getTypeDefinitions(compilationUnit.getTypes(), compilationUnit.getCallableCodeBlocks()) +
                getSymbolDefinitions(compilationUnit.getSymbols(), ";\n", ";\n");

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

    private String getExternSymbolDeclarations(List<? extends Symbol> symbols, String separator, String trailing) {
        return getSymbolDeclarations(symbols, separator, trailing, true);
    }

    private String getSymbolDeclarations(List<? extends Symbol> symbols, String separator, String trailing) {
        return getSymbolDeclarations(symbols, separator, trailing, false);
    }

    private String getSymbolDefinitions(List<? extends Symbol> symbols, String separator, String trailing) {
        return getSymbolDeclarations(symbols, separator, trailing, false);
    }

    private String getSymbolDeclarations(List<? extends Symbol> symbols, String separator, String trailing, boolean asExternals) {
        String output = symbols.stream().map(symbol -> getSymbolDeclaration(symbol, asExternals))
                .collect(Collectors.joining(separator));

        if(output.isEmpty()) {
            return output;
        } else {
            return output + trailing;
        }
    }

    private String getTypeForwardDeclarations(List<? extends Type> types, List<CallableCode> callableCodeBlocks) {
        return types.stream().map( type -> getTypeForwardDeclaration(type, callableCodeBlocks))
                .collect(Collectors.joining());
    }

    private String getTypeDefinitions(List<? extends Type> types, List<CallableCode> callableCodeBlocks) {
        String output = types.stream().map(type -> getTypeDefinition(type, callableCodeBlocks)).collect(Collectors.joining());

        if(output.isEmpty()) {
            return output;
        } else {
            return output + "\n";
        }
    }

    private String getSymbolDeclaration(Symbol symbol, boolean asExtern) {
        if(symbol instanceof NamedValueSymbol valueSymbol) {
            String asterisks = asterisks(valueSymbol.getType().getArrayDimensions());
            String cVariableName = asterisks + valueSymbol.getName();

            if(valueSymbol.isFunction()) {
                CallableType callableType = Objects.requireNonNull((CallableType) valueSymbol.getType().getType());
                if(asExtern) {
                    return String.format("extern %s %s", callableType.getName(), cVariableName);
                } else if(valueSymbol.getValue() == null) {
                    return String.format("%s %s", callableType.getName(), cVariableName);
                } else {
                    return String.format("%s %s = %s", callableType.getName(), cVariableName, getCExpression(valueSymbol.getValue()));
                }
            } else {
                TypeReference type = valueSymbol.getType();
                var cExpression = Optional.ofNullable(valueSymbol.getValue())
                        .map(this::getCExpression)
                        .orElse("");

                String cTypeName =
                        getCTypeName(type.getName());

                if(asExtern) {
                    return String.format("extern %s %s", cTypeName, cVariableName);
                } else if(valueSymbol.getValue() == null) {
                    return String.format("%s %s", cTypeName, cVariableName);
                } else {
                    return String.format("%s %s = %s", cTypeName, cVariableName, cExpression);
                }
            }
        } else {
            throw new IllegalArgumentException("Unknown symbol " + symbol);
        }
    }

    private static String asterisks(int arrayDimensions) {
        return IntStream.range(0, arrayDimensions)
                .mapToObj(_ -> "*")
                .collect(Collectors.joining());
    }

    private String getCTypeName(String typeName) {
        return switch(typeName) {
            case "boolean" -> "bool";
            case "any" -> "void*";
            default -> typeName;
        };
    }

    private String getTypeForwardDeclaration(Type type, List<CallableCode> callableCodeBlocks) {
        if(type instanceof CallableType callableType) {
            String returnType = getCallableTypeReturnType(callableType);
            String parameterList = getCallableTypeParameterList(callableType);

            Optional<CallableCode> callableCode = callableCodeBlocks
                    .stream()
                    .filter(codeItem -> codeItem.getType().getName().equals(type.getName()))
                    .findFirst();

            if(callableCode.isEmpty()) {
                return String.format("typedef %s (*%s)(%s);\n",
                        returnType, callableType.getName(), parameterList);
            } else {
                String varArgs = callableType.isVarArgs() ? ", ...": "";
                return callableCode.map(code -> String.format("typedef %s (*%s)(%s%s);\n%s %s(%s%s);\n",
                                returnType, callableType.getName(), parameterList, varArgs,
                                returnType, code.getName(), parameterList, varArgs))
                        .orElse("");
            }


        } else {
            return "";
        }
    }

    private String getTypeDefinition(Type type, List<CallableCode> callableCodeBlocks) {
        if(type instanceof CallableType callableType) {
            String returnType = getCallableTypeReturnType(callableType);
            String parameterList = getCallableTypeParameterList(callableType);

            Optional<CallableCode> callableCode = callableCodeBlocks
                    .stream()
                    .filter(codeItem -> codeItem.getType().getName().equals(type.getName()))
                    .findFirst();

            return callableCode.map(code -> String.format("%s %s(%s) {%s}",
                        returnType, code.getName(), parameterList,
                        getCExpressions(Objects.requireNonNull(code.getStatementBlock()).getStatements(), ";\n", ";\n")))
                    .orElse("");
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
        if(expression instanceof CallableCodeExpression callableCodeExpression) {
            return callableCodeExpression.getCallableCode().getName();
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
        } else if(expression instanceof CompareGreaterThanEqualToExpression operator) {
            return String.format("(%s) >= (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof CompareLessThanExpression operator) {
            return String.format("(%s) < (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        } else if(expression instanceof CompareLessThanEqualToExpression operator) {
            return String.format("(%s) <= (%s)", getCExpression(operator.getLeft()), getCExpression(operator.getRight()));
        }  else if(expression instanceof CompareNotEqualToExpression operator) {
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
            String output = getCExpressions(blockExpression.getStatements(), ";\n", ";\n");

            if(output.isEmpty()) {
                return String.format("{%s}", output);
            } else {
                return String.format("{%s}\n", output);
            }
        } else if (expression instanceof FunctionCallExpression functionCallExpression) {
            String arguments = functionCallExpression.getArguments().stream().map(Argument::getExpression)
                    .map(this::getCExpression)
                    .map(cExpression -> String.format("%s", cExpression))
                    .collect(Collectors.joining(", "));

            String callableCExpression = getCExpression(functionCallExpression.getCallableExpression());
            return String.format("%s(%s)", callableCExpression, arguments);
        } else if(expression instanceof DoWhileLoop doWhileLoop) {
            String statements = getCExpression(doWhileLoop.getBlockExpression());
            String booleanCheck = getCExpression(doWhileLoop.getBooleanExpression());
            return String.format("do %s while(%s);", statements, booleanCheck);
        } else if(expression instanceof ForLoop forLoop) {
            String initialisationExpression = Optional
                    .ofNullable(forLoop.getDeclarationExpression())
                    .map(this::getCExpression).orElse("");
            String comparisonExpression = Optional
                    .ofNullable(forLoop.getComparisonExpression())
                    .map(this::getCExpression).orElse("");
            String counterExpression = Optional
                    .ofNullable(forLoop.getCounterExpression())
                    .map(this::getCExpression).orElse("");

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
        } else if(expression instanceof ExplicitCastExpression explicitCastExpression) {
            String operandCExpression = getCExpression(explicitCastExpression.getOperand());
            TypeReference operandType = Objects.requireNonNull(explicitCastExpression.getOperand().getType());
            TypeReference targetType = Objects.requireNonNull(explicitCastExpression.getType());
            String targetTypeName = targetType.getName();
            String targetCType = getCTypeName(targetTypeName) + asterisks(targetType.getArrayDimensions());

            if(targetTypeName.equals("any")) {
                if(operandType.getArrayDimensions() > 0 || operandType.getType() instanceof  CallableType) {
                    return String.format("(%s)(%s)", targetCType, operandCExpression);
                } else {
                    return String.format("(&%s)", operandCExpression);
                }
            } else if(operandType.getName().equals("any")) {
                if(targetType.getArrayDimensions() > 0 || targetType.getType() instanceof CallableType) {
                    return String.format("(%s)(%s)", targetCType, operandCExpression);
                } else {
                    return String.format("*((%s*)(%s))", targetCType, operandCExpression);
                }
            } else {
                return String.format("(%s)(%s)", targetCType, operandCExpression);
            }
        } else if(expression instanceof TypePassExpression typePassExpression) {
            return typePassExpression.getTypeReference().getName();
        } else if(expression instanceof VariableDeclaration variableDeclaration) {
            return getSymbolDeclaration(variableDeclaration.getNamedValueSymbol(), false);
        }else {
            logs.add(LogEntry.warning(expression.getSource(), LogEntryCode.UNKNOWN_EXPRESSION));
            return "";
        }
    }

    private String getCallableTypeReturnType(CallableType callableType) {

        if(callableType.getReturnType() == null) {
            return "void";
        } else if(callableType.getReturnType().getType() instanceof CallableType returnTypeCallable) {
            return returnTypeCallable.getName();
        }else {
            String pointers = asterisks(callableType.getReturnType().getArrayDimensions());
            return getCTypeName(callableType.getReturnType().getName()) + pointers;
        }
    }

    private String getCallableTypeParameterList(CallableType callableType) {
        return getSymbolDeclarations(callableType.getParameters(), ", ", "");
    }
}
