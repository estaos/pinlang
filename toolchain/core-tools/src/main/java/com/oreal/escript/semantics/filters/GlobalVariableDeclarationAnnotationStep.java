package com.oreal.escript.semantics.filters;

import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.logging.LogEntry;
import com.oreal.escript.semantics.Scope;

import java.util.List;

public class GlobalVariableDeclarationAnnotationStep implements AnnotationStep {
    private static final Annotations annotations = new Annotations();

    @Override
    public void annotate(CompilationUnit compilationUnit, Scope scope, List<LogEntry> logs) {
        for(Symbol symbol : compilationUnit.getSymbols()) {
            if(symbol instanceof NamedValueSymbol variableDeclaration) {
                if(variableDeclaration.getType() == null) {
                    annotations.inferVariableType(variableDeclaration, scope, logs);
                }

                if(variableDeclaration.getType() != null) {
                    annotations.resolveVariableTypeReference(variableDeclaration, scope, logs);
                    annotations.checkValueTypeIsVariableType(variableDeclaration, scope, logs);
                }
            }
        }
    }
}
