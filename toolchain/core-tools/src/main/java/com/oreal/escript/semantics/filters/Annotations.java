package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.Argument;
import com.oreal.escript.parser.ast.AssignmentExpression;
import com.oreal.escript.parser.ast.BinaryOperatorExpression;
import com.oreal.escript.parser.ast.BitwiseNotExpression;
import com.oreal.escript.parser.ast.BlockExpression;
import com.oreal.escript.parser.ast.BooleanLiteralExpression;
import com.oreal.escript.parser.ast.CallableCode;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CharLiteralExpression;
import com.oreal.escript.parser.ast.CharSequenceLiteralExpression;
import com.oreal.escript.parser.ast.CompareEqualToExpression;
import com.oreal.escript.parser.ast.CompareGreaterThanEqualToExpression;
import com.oreal.escript.parser.ast.CompareGreaterThanExpression;
import com.oreal.escript.parser.ast.CompareLessThanEqualToExpression;
import com.oreal.escript.parser.ast.CompareLessThanExpression;
import com.oreal.escript.parser.ast.CompareNotEqualToExpression;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.ExplicitCastExpression;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.FunctionCallExpression;
import com.oreal.escript.parser.ast.IfStatement;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.LogicalAndExpression;
import com.oreal.escript.parser.ast.LogicalNotExpression;
import com.oreal.escript.parser.ast.LogicalOrExpression;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NullExpression;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.ReturnStatement;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.SymbolValueExpression;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypePassExpression;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.parser.ast.VariableDeclaration;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.parser.logging.LogEntryCode;
import com.oreal.escript.semantics.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class Annotations {
    /// Resolves the Type for variables that have their types explicitly defined.
    ///
    /// If the variable has a value assigned, it is important the caller checks that the value matches this resolved
    /// type.
    public void resolveVariableTypeReference(NamedValueSymbol variable, Scope scope, List<LogEntry> logs) {
        @Nullable Type type = scope.resolveType(variable.getType().getName());
        if(type != null) {
            variable.getType().setType(type);
        } else {
            logs.add(LogEntry.error(variable.getSource(), LogEntryCode.UNKNOWN_TYPE_REFERENCE));
        }
    }

    public void resolveReturnType(CallableType callableType, Scope scope, List<LogEntry> logs) {
        TypeReference returnTypeReference = Objects.requireNonNull(callableType.getReturnType());
        @Nullable Type type = scope.resolveType(returnTypeReference.getName());
        if(type != null) {
            callableType.getReturnType().setType(type);
        } else {
            logs.add(LogEntry.error(callableType.getSource(), LogEntryCode.UNKNOWN_TYPE_REFERENCE));
        }
    }

    public void inferVariableType(NamedValueSymbol variable, Scope scope, List<LogEntry> logs) {
        if(variable.getValue() == null) {
            logs.add(LogEntry.error(variable.getSource(), LogEntryCode.NO_TYPE_INFORMATION));
        } else {
            @Nullable TypeReference type = visitExpression(variable.getValue(), scope, logs);
            if(type == null) {
                logs.add(LogEntry.error(variable.getSource(), LogEntryCode.VALUE_HAS_NO_TYPE));
            } else {
                variable.setType(type);
            }
        }
    }

    public void checkValueTypeIsVariableType(NamedValueSymbol namedValueSymbol, Scope scope, List<LogEntry> logs) {
        if(namedValueSymbol.getType() != null) {
            @Nullable TypeReference typeReference = getExpressionTypeInFavorOfHint(namedValueSymbol.getValue(), namedValueSymbol.getType(), scope, logs);
            if(typeReference == null
                    || !namedValueSymbol.getType().getName().equals(typeReference.getName())
                    || namedValueSymbol.getType().getArrayDimensions() != typeReference.getArrayDimensions()) {
                logs.add(LogEntry.error(namedValueSymbol.getSource(), LogEntryCode.TYPE_MISMATCH));
            }
        }
    }

    public void requireConstExpression(Expression expression, List<LogEntry> logs) {
        if(!expression.isConstExpression()) {
            logs.add(LogEntry.error(expression.getSource(), LogEntryCode.EXPRESSION_IS_NOT_CONST));
        }
    }

    // Applied to all explicit cast expressions
    public void checkExplicitCastIsValid(ExplicitCastExpression expression, Scope scope, List<LogEntry> logs) {
        TypeReference targetType = Objects.requireNonNull(expression.getType());
        Expression operand = expression.getOperand();

        if(targetType.getName().equals("any")) {
            if(!(operand instanceof SymbolValueExpression)) {
                // If casting to any, operand must be a variable name
                logs.add(LogEntry.error(expression.getSource(), LogEntryCode.CAST_OPERAND_HAS_NO_ADDRESS));
            }
        } else if(scope.isForProject()) {
            // Not allowed to cast to types other than any outside of project scope
            logs.add(LogEntry.error(expression.getSource(), LogEntryCode.EXPRESSION_IS_NOT_CONST));
        } else {
            // All other castings are valid if type being cast to is a subtype of type being cast from
            @Nullable TypeReference operandType = getExpressionTypeInFavorOfHint(operand, targetType, scope, logs);
            if(operandType == null) {
                logs.add(LogEntry.error(expression.getSource(), LogEntryCode.NO_TYPE_INFORMATION));
            } else {
                if(!isSubType(targetType, operandType)) {
                    logs.add(LogEntry.error(expression.getSource(), LogEntryCode.TYPE_MISMATCH));
                }
            }
        }
    }

    public void visitBlockExpression(BlockExpression blockExpression, Scope parentScope, List<LogEntry> logs, @Nullable Type expectedReturnValue) {
        Scope scope = new Scope(parentScope, "", false);

        for(Expression statement : blockExpression.getStatements()) {
            visitExpression(statement, scope, logs);

            if(statement instanceof ReturnStatement returnStatement) {
                if(expectedReturnValue == null) {
                    logs.add(LogEntry.error(statement.getSource(),
                            LogEntryCode.RETURNING_FROM_VOID_CONTEXT, "Cannot return from void", null));
                } else {
                    @Nullable Type returnType = Objects.requireNonNull(returnStatement.getReturnExpression().getType()).getType();
                    if(returnType == null || !returnType.isSubTypeOf(expectedReturnValue)) {
                        logs.add(LogEntry.error(statement.getSource(),
                                LogEntryCode.TYPE_MISMATCH, String.format("Expression does not fulfil expected return type %s", expectedReturnValue.getName()), null));
                    }
                }
            } else if(statement instanceof BlockExpression innerBlock) {
                visitBlockExpression(innerBlock, scope, logs, expectedReturnValue);
            } else if(statement instanceof IfStatement ifStatement) {
                expectBoolean(ifStatement.getBooleanExpression(), scope, logs);
                visitBlockExpression(ifStatement.getBlockExpression(), scope, logs, expectedReturnValue);

                for(IfStatement.ElseIfBlock elseIfBlock : ifStatement.getElseIfBlocks()) {
                    expectBoolean(elseIfBlock.getBooleanExpression(), scope, logs);
                    visitBlockExpression(elseIfBlock.getBlockExpression(), scope, logs, expectedReturnValue);
                }

                if(ifStatement.getElseBlockExpression() != null) {
                    visitBlockExpression(ifStatement.getElseBlockExpression(), scope, logs, expectedReturnValue);
                }
            }
        }
    }

    public void visitCallableCode(CallableCode callableCode, Scope projectScope, List<LogEntry> logs) {
        @Nullable Type returnType = Optional
                .ofNullable(callableCode.getType().getReturnType())
                .map(TypeReference::getType)
                .orElse(null);

        Scope functionScope = new Scope(projectScope, "", false);
        for(Symbol parameter: callableCode.getType().getParameters()) {
            if(parameter instanceof NamedValueSymbol namedValueSymbol) {
                var declaration = new VariableDeclaration(callableCode.getSource(), namedValueSymbol);
                visitExpression(declaration, functionScope, logs);
            } else {
                throw new UnsupportedOperationException("Unknown symbol type "+ parameter);
            }
        }

        visitBlockExpression(callableCode.getStatementBlock(), functionScope, logs, returnType);
    }

    /// Visits given expression, applies validation rules and returns the type this expression resolves to
    public @Nullable TypeReference visitExpression(Expression expression, Scope scope, List<LogEntry> logs) {
        if(expression.getType() != null && expression.getType().getType() != null) {
            return expression.getType();
        } else {
            @Nullable TypeReference typeReference = null;
            switch (expression) {
                case NumberLiteralExpression numberLiteralExpression -> {
                    Type smallestFittingType = getSmallestFittingTypeForNumberLiteral(numberLiteralExpression, scope);
                    typeReference = TypeReference.ofType(smallestFittingType);
                }
                case BooleanLiteralExpression _ -> {
                    Type booleanType = scope.resolveType("boolean");
                    typeReference = TypeReference.ofType(booleanType);
                }
                case CharLiteralExpression _ -> {
                    Type charType = scope.resolveType("char");
                    typeReference = TypeReference.ofType(charType);
                }
                case CharSequenceLiteralExpression _ -> {
                    Type charSequenceType = scope.resolveType("char");
                    typeReference = TypeReference.ofType(charSequenceType, 1);
                }
                case NullExpression _ -> {
                    Type anyType = scope.resolveType("any");
                    typeReference = TypeReference.ofType(anyType);
                }
                case SymbolValueExpression symbolValueExpression -> {
                    Symbol symbol = scope.resolveSymbol(symbolValueExpression.getSymbolName());
                    if(symbol == null) {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.NO_SUCH_SYMBOL));
                    } else {
                        typeReference = symbol.getType();
                    }
                }
                case ExplicitCastExpression explicitCastExpression -> {
                    visitExpression(explicitCastExpression.getOperand(), scope, logs);
                    TypeReference targetType = explicitCastExpression.getType();
                    @Nullable Type type = scope.resolveType(targetType.getName());
                    if(type == null) {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.NO_SUCH_SYMBOL));
                    } else {
                        typeReference = TypeReference.ofType(type, targetType.getArrayDimensions());
                        explicitCastExpression.setType(typeReference);
                        checkExplicitCastIsValid(explicitCastExpression, scope, logs);
                    }
                }
                case TypePassExpression typePassExpression -> {
                    @Nullable Type type = scope.resolveType(typePassExpression.getTypeReference().getName());
                    if(type == null) {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.NO_SUCH_SYMBOL));
                    }

                    typeReference = typePassExpression.getType();
                }
                case ReturnStatement returnStatement -> {
                    visitExpression(returnStatement.getReturnExpression(), scope, logs);
                    typeReference = returnStatement.getType();
                }
                case FunctionCallExpression functionCallExpression -> {
                    Expression callableExpression = functionCallExpression.getCallableExpression();
                    visitExpression(callableExpression, scope, logs);
                    for(Argument argument: functionCallExpression.getArguments()) {
                        visitExpression(argument.getExpression(), scope, logs);
                    }

                    @Nullable Type type = Optional.ofNullable(callableExpression.getType())
                            .map(TypeReference::getType)
                            .orElse(null);
                    if(!(type instanceof CallableType callableType)) {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.EXPRESSION_IS_NOT_CALLABLE));
                    } else {
                        callableCanBeCalledWithArguments(expression.getSource(), callableType,
                                functionCallExpression.getArguments(), logs);

                        typeReference = callableType.getReturnType();
                    }
                }
                case VariableDeclaration variableDeclaration -> {
                    NamedValueSymbol symbol = variableDeclaration.getNamedValueSymbol();
                    boolean alreadyInScope = scope.nameAlreadyInScope(symbol.getName());
                    if(alreadyInScope) {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.SYMBOL_ALREADY_IN_SCOPE));
                    } else {
                        if(symbol.getType() == null) {
                            inferVariableType(symbol, scope, logs);
                        }

                        if(symbol.getType() != null) {
                            resolveVariableTypeReference(symbol, scope, logs);
                            if(symbol.getValue() != null) {
                                checkValueTypeIsVariableType(symbol, scope, logs);
                            }
                        }
                    }

                    scope.registerSymbol(symbol);
                    typeReference = variableDeclaration.getType();
                }
                case AssignmentExpression assignmentExpression -> {
                    @Nullable Symbol symbol = scope.resolveSymbol(assignmentExpression.getSymbolName());
                    if(symbol == null) {
                        boolean alreadyInScope = scope.nameAlreadyInScope(assignmentExpression.getSymbolName());
                        if(alreadyInScope) {
                            logs.add(LogEntry.error(expression.getSource(), LogEntryCode.TYPE_MISMATCH));
                        } else {
                            @Nullable TypeReference valueType = visitExpression(assignmentExpression.getValue(), scope, logs);
                            if(valueType == null) {
                                logs.add(LogEntry.error(expression.getSource(), LogEntryCode.VALUE_HAS_NO_TYPE));
                            } else if(
                                    !getExpressionTypeInFavorOfHint(assignmentExpression.getValue(), symbol.getType(), scope, logs)
                                            .equals(symbol.getType().getName())) {
                                logs.add(LogEntry.error(expression.getSource(), LogEntryCode.TYPE_MISMATCH));
                            } else {
                                typeReference = symbol.getType();
                            }
                        }
                    } else {
                        logs.add(LogEntry.error(expression.getSource(), LogEntryCode.SYMBOL_ALREADY_IN_SCOPE));
                    }
                }
                case LogicalNotExpression logicalNotExpression -> {
                    expectBoolean(logicalNotExpression.getOperand(), scope, logs);
                    typeReference = logicalNotExpression.getType();
                }
                case BitwiseNotExpression bitwiseNotExpression -> {
                    expectNumber(bitwiseNotExpression.getOperand(), scope, logs);
                    typeReference = bitwiseNotExpression.getType();
                }
                case BinaryOperatorExpression binaryOperatorExpression -> {
                    if(binaryOperatorExpression instanceof LogicalAndExpression
                        || binaryOperatorExpression instanceof LogicalOrExpression) {
                        expectBoolean(binaryOperatorExpression.getLeft(), scope, logs);
                        expectBoolean(binaryOperatorExpression.getRight(), scope, logs);
                    } else {
                        expectNumber(binaryOperatorExpression.getLeft(), scope, logs);
                        expectNumber(binaryOperatorExpression.getRight(), scope, logs);
                    }

                    if(binaryOperatorExpression instanceof CompareLessThanExpression
                        || binaryOperatorExpression instanceof CompareLessThanEqualToExpression
                        || binaryOperatorExpression instanceof CompareEqualToExpression
                        || binaryOperatorExpression instanceof CompareGreaterThanExpression
                        || binaryOperatorExpression instanceof CompareGreaterThanEqualToExpression
                        || binaryOperatorExpression instanceof CompareNotEqualToExpression
                        || binaryOperatorExpression instanceof LogicalOrExpression
                        || binaryOperatorExpression instanceof LogicalAndExpression) {
                        Type booleanType = scope.resolveType("boolean");
                        typeReference = TypeReference.ofType(booleanType);
                    } else {
                        typeReference = getNumberBinaryExpressionType(binaryOperatorExpression, scope);
                    }
                }
                default -> {
                }
            }

            // TODO: add more inference rules. I.e how do we infer return type of operators?

            expression.setType(typeReference);
            return typeReference;
        }
    }

    public void addSymbolsToScope(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs) {
        if(!compilationUnit.isAddedToGlobalScope()) {
            for(Import importItem : compilationUnit.getImports()) {
                addSymbolsToScope(Objects.requireNonNull(importItem.getCompilationUnit()), scope, logs);
            }

            for(Symbol symbol : compilationUnit.getSymbols()) {
                addSymbolToScope(symbol, scope, logs);
            }

            for(Type type : compilationUnit.getTypes()) {
                addTypeToScope(type, scope, logs);
            }

            compilationUnit.setAddedToGlobalScope(true);
        }
    }

    public void addTypeToScope(Type type, Scope scope, List<LogEntry> logs) {
        try {
            scope.registerType(type);
        } catch (IllegalStateException exception) {
            logs.add(LogEntry.error(type.getSource(), LogEntryCode.SYMBOL_ALREADY_IN_SCOPE));
        }
    }

    public void addSymbolToScope(Symbol symbol, Scope scope, List<LogEntry> logs) {
        try {
            scope.registerSymbol(symbol);
        } catch (IllegalStateException exception) {
            logs.add(LogEntry.error(symbol.getSource(), LogEntryCode.SYMBOL_ALREADY_IN_SCOPE));
        }
    }

    /// Returns the type of the expression in favor of the hint.
    ///
    /// If the type of the expression is a subtype of the hint, then the hint is returned,
    /// otherwise the expression is returned.
    ///
    /// Returns null if expression type could not be determined.
    private @Nullable TypeReference getExpressionTypeInFavorOfHint(Expression expression, TypeReference hint, Scope scope, List<LogEntry> logs) {
        @Nullable TypeReference expressionType = visitExpression(expression, scope, logs);
        if(expressionType != null) {
            if(isSubType(expressionType, hint)) {
                return hint;
            } else {
                return expressionType;
            }
        } else {
            return null;
        }
    }

    private boolean isSubType(TypeReference typeA, TypeReference typeB) {
        if(typeA.getType() != null && typeB.getType() != null) {
            return typeA.getType().isSubTypeOf(typeB.getType()) && typeA.getArrayDimensions() == typeB.getArrayDimensions();
        } else {
            return false;
        }
    }

    private Type getSmallestFittingTypeForNumberLiteral(NumberLiteralExpression expression, Scope scope) {
        if(expression.isDecimal()) {
            var doubleValue = Double.parseDouble(expression.getNumberAsString());
            if(doubleValue > Float.MAX_VALUE) {
                return scope.resolveType("double");
            } else {
                return scope.resolveType("float");
            }
        } else {
            var value = Long.parseLong(expression.getNumberAsString());
            if(value > Integer.MAX_VALUE) {
                return scope.resolveType("int64");
            } else if(value > Short.MAX_VALUE) {
                return scope.resolveType("int32");
            } else if(value > Byte.MAX_VALUE) {
                return scope.resolveType("int16");
            } else {
                return scope.resolveType("int8");
            }
        }
    }

    private void expectBoolean(Expression expression, Scope scope, List<LogEntry> logs) {
        @Nullable TypeReference expressionType = visitExpression(expression, scope, logs);
        if(expressionType == null
                || expressionType.getArrayDimensions() > 0
                || !expressionType.getName().equals("boolean")) {
            logs.add(LogEntry.error(expression.getSource(), LogEntryCode.EXPRESSION_IS_NOT_BOOLEAN));
        }
    }

    private void expectNumber(Expression expression, Scope scope, List<LogEntry> logs) {
        @Nullable TypeReference expressionType = visitExpression(expression, scope, logs);
        if(expressionType == null
                || expressionType.getArrayDimensions() > 0
                || !List.of("double", "float", "int512", "int256", "int128",
                "int64", "int32", "int16", "int8", "char").contains(expressionType.getName())) {
            logs.add(LogEntry.error(expression.getSource(), LogEntryCode.EXPRESSION_IS_NOT_NUMBER));
        }
    }

    private @Nullable TypeReference getNumberBinaryExpressionType(BinaryOperatorExpression expression, Scope scope) {
        @Nullable TypeReference leftType = expression.getLeft().getType();
        @Nullable TypeReference rightType = expression.getRight().getType();

        Type doubleType = Objects.requireNonNull(scope.resolveType("double"));
        Type int64Type = Objects.requireNonNull(scope.resolveType("int64"));

        if(leftType == null || leftType.getType() == null || rightType == null || rightType.getType() == null) {
            return null;
        } else if(leftType.getType().isSubTypeOf(rightType.getType())) {
            return rightType;
        } else if(rightType.getType().isSubTypeOf(leftType.getType())) {
            return leftType;
        } else if(leftType.getType().isSubTypeOf(doubleType) || rightType.getType().isSubTypeOf(doubleType)) {
            return TypeReference.ofType(doubleType);
        } else {
            return TypeReference.ofType(int64Type);
        }
    }


    private void callableCanBeCalledWithArguments(Source source, CallableType callableType, List<Argument> arguments, List<LogEntry> logs) {
        int minimumArgumentCount = Math.min(arguments.size(), callableType.getParameters().size());
        for(int index = 0; index < minimumArgumentCount; index++) {
            Argument argument = arguments.get(index);
            Symbol parameter = callableType.getParameters().get(index);

            Type argumentType = Objects.requireNonNull(Objects.requireNonNull(argument.getExpression().getType()).getType());
            Type parameterType = Objects.requireNonNull(parameter.getType().getType());

            if(!isSubType(TypeReference.ofType(parameterType), TypeReference.ofType(argumentType))) {
                logs.add(LogEntry.error(
                        source, LogEntryCode.TYPE_MISMATCH,
                        String.format("Cannot assign %s to %s", argumentType.getName(), parameterType.getName()),
                        null));
            }
        }

        if(arguments.size() < callableType.getParameters().size()) {
            logs.add(LogEntry.error(
                    source, LogEntryCode.NOT_ENOUGH_ARGUMENTS_TO_FUNCTION,
                    String.format("Not enough arguments to %s", callableType.getName()),
                    null));
        } else if(!callableType.isVarArgs() && arguments.size() > callableType.getParameters().size()) {
            logs.add(LogEntry.error(
                    source, LogEntryCode.TOO_MANY_ARGUMENTS_TO_FUNCTIONS,
                    String.format("Too many arguments to %s", callableType.getName()),
                    null));
        }
    }
}
