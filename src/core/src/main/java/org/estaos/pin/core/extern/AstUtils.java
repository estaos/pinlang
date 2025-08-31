package org.estaos.pin.core.extern;

import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.Source;
import org.estaos.pin.core.parser.ast.Type;
import org.estaos.pin.core.parser.ast.TypeReference;

import java.io.File;

public class AstUtils {
    public static NamedValueSymbol getNamedValueSymbol(File file, String name, String typeName, int arrayDimensions) {
        return new NamedValueSymbol(
                name,
                TypeReference.ofType(typeName, arrayDimensions),
                Source.defaultSource(file),
                true, false, "",
                null, false
        );
    }

    public static NamedValueSymbol getNamedValueSymbol(File file, String name, Type type, int arrayDimensions) {
        return new NamedValueSymbol(
                name,
                TypeReference.ofType(type, arrayDimensions),
                Source.defaultSource(file),
                true, false, "",
                null, false
        );
    }
}
