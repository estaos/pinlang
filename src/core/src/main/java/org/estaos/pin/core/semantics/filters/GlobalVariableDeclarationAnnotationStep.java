package org.estaos.pin.core.semantics.filters;

import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.Symbol;
import org.estaos.pin.core.parser.logging.LogEntry;
import org.estaos.pin.core.semantics.Scope;

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

                    if(variableDeclaration.getValue() != null) {
                        // Global scope values must be constants and value types must match type specified
                        annotations.requireConstExpression(variableDeclaration.getValue(), logs);
                        annotations.checkValueTypeIsVariableType(variableDeclaration, scope, logs);
                    }
                }
            }
        }
    }
}
