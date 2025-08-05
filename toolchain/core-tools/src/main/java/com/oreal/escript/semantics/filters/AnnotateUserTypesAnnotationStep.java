package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.CallableCode;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.Scope;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/// Resolves the type references for functions, structs and other user defined types
public class AnnotateUserTypesAnnotationStep implements AnnotationStep {
    private static final Annotations annotations = new Annotations();

    @Override
    public void annotate(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs) {
        for(Import importItem : compilationUnit.getImports()) {
            annotate(Objects.requireNonNull(importItem.getCompilationUnit()), scope, logs);
        }

        for(Type type : compilationUnit.getTypes()) {
            if(type instanceof CallableType callableType) {
                if(callableType.getReturnType() != null) {
                    annotations.resolveReturnType(callableType, scope, logs);
                }

                for(Symbol parameter : callableType.getParameters()) {
                    if(parameter instanceof NamedValueSymbol namedValueSymbol) {
                        annotations.resolveVariableTypeReference(namedValueSymbol, scope, logs);
                    } else {
                        throw new IllegalArgumentException("Unknown symbol " + parameter);
                    }
                }
            }
        }

        for(CallableCode callableCode: compilationUnit.getCallableCodeBlocks()) {
            @Nullable Type returnType = Optional
                    .ofNullable(callableCode.getType().getReturnType())
                    .map(TypeReference::getType)
                    .orElse(null);
            annotations.visitBlockExpression(callableCode.getStatementBlock(), scope, logs, returnType);
        }
    }
}
