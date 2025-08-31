package org.estaos.pin.core.semantics.filters;

import org.estaos.pin.core.parser.ast.CallableCode;
import org.estaos.pin.core.parser.ast.CallableType;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.Import;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.Symbol;
import org.estaos.pin.core.parser.ast.Type;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.semantics.Scope;

import java.util.List;
import java.util.Objects;

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
            annotations.visitCallableCode(callableCode, scope.findProjectScope(), logs);
        }
    }
}
