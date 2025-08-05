package com.oreal.escript.extern;

import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypeReference;

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
