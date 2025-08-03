package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.BooleanLiteralExpression;
import com.oreal.escript.parser.ast.CharLiteralExpression;
import com.oreal.escript.parser.ast.CharSequenceLiteralExpression;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NullExpression;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.SymbolValueExpression;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.parser.logging.LogEntryCode;
import com.oreal.escript.semantics.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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

    public void inferVariableType(NamedValueSymbol variable, Scope scope, List<LogEntry> logs) {
        if(variable.getValue() == null) {
            logs.add(LogEntry.error(variable.getSource(), LogEntryCode.NO_TYPE_INFORMATION));
        } else {
            @Nullable TypeReference type = resolveExpressionType(variable.getValue(), scope, logs);
            if(type == null) {
                logs.add(LogEntry.error(variable.getSource(), LogEntryCode.VALUE_HAS_NO_TYPE));
            } else {
                variable.setType(type);
            }
        }
    }

    public void checkValueTypeIsVariableType(NamedValueSymbol namedValueSymbol, Scope scope, List<LogEntry> logs) {
        if(namedValueSymbol.getType() != null && namedValueSymbol.getValue() != null) {
            @Nullable TypeReference typeReference = getExpressionTypeInFavorOfHint(namedValueSymbol.getValue(), namedValueSymbol.getType(), scope, logs);
            if(typeReference == null || !namedValueSymbol.getType().getName().equals(typeReference.getName())) {
                logs.add(LogEntry.error(namedValueSymbol.getSource(), LogEntryCode.TYPE_MISMATCH));
            }
        }
    }

    public @Nullable TypeReference resolveExpressionType(Expression expression, Scope scope, List<LogEntry> logs) {
        if(expression.getType() != null) {
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
                    // TODO: circle back on this as it should be type char[] instead of char.
                    // Need to update type references to be the ones that denote an array type reference or not instead of the symbol
                    Type charSequenceType = scope.resolveType("char");
                    typeReference = TypeReference.ofType(charSequenceType);
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
                default -> {
                }
            }

            // TODO: add more inference rules. I.e how do we infer return type of operators?

            expression.setType(typeReference);
            return typeReference;
        }
    }

    public void addSymbolsToScope(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs) {
        for(Import importItem : compilationUnit.getImports()) {
            addSymbolsToScope(Objects.requireNonNull(importItem.getCompilationUnit()), scope, logs);
        }

        for(Symbol symbol : compilationUnit.getSymbols()) {
            addSymbolToScope(symbol, scope, logs);
        }

        for(Type type : compilationUnit.getTypes()) {
            addTypeToScope(type, scope, logs);
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
    private@Nullable TypeReference getExpressionTypeInFavorOfHint(Expression expression, TypeReference hint, Scope scope, List<LogEntry> logs) {
        @Nullable TypeReference expressionType = resolveExpressionType(expression, scope, logs);
        if(expressionType != null && expressionType.getType() != null ) {
            if(expressionType.getType().isSubTypeOf(hint.getType())) {
                return hint;
            } else {
                return expressionType;
            }
        } else {
            return null;
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
}
