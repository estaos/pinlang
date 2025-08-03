package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.Scope;

import java.util.List;

/// Resolves the type references for functions, structs and other user defined types
public class AnnotateUserTypesAnnotationStep implements AnnotationStep {
    private static final Annotations annotations = new Annotations();

    @Override
    public void annotate(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs) {
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
    }
}
