package org.estaos.pin.core.parser.ast;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class BasicType extends Type {

    // Basic type names
    public static final String INT8 = "int8";
    public static final String INT16 = "int16";
    public static final String INT32 = "int32";
    public static final String INT64 = "int64";
    public static final String CHAR = "char";
    public static final String BOOLEAN = "boolean";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";

    private static final Set<String> INTEGER_TYPES = Set.of(INT8, INT16, INT32, INT64);
    private static final Set<String> FLOAT_TYPES = Set.of(FLOAT, DOUBLE);

    public BasicType(
            @Nullable Source source,
            String name,
            String documentationMarkdown) {
        super(source, name, List.of(), documentationMarkdown, List.of());
    }

    @Override
    public boolean canBeCastedTo(Type other) {
        if (!(other instanceof BasicType otherBasic)) return false;

        String thisName = this.getName();
        String otherName = otherBasic.getName();
        
        // A basic type can be casted to itself
        if (thisName.equals(otherName)) return true;
        
        // char can be casted to and from int8
        if ((thisName.equals(CHAR) && otherName.equals(INT8)) ||
            (thisName.equals(INT8) && otherName.equals(CHAR))) return true;
        
        // Integer types can be casted to other integer types
        if (INTEGER_TYPES.contains(thisName) && INTEGER_TYPES.contains(otherName)) return true;
        
        // char can be casted to any integer type (via int8)
        if (thisName.equals(CHAR) && INTEGER_TYPES.contains(otherName)) return true;
        if (INTEGER_TYPES.contains(thisName) && otherName.equals(CHAR)) return true;
        
        // Float types can be casted to and from integer types
        if (FLOAT_TYPES.contains(thisName) && INTEGER_TYPES.contains(otherName)) return true;
        if (INTEGER_TYPES.contains(thisName) && FLOAT_TYPES.contains(otherName)) return true;
        
        // double can be casted to and from float
        if ((thisName.equals(DOUBLE) && otherName.equals(FLOAT)) ||
            (thisName.equals(FLOAT) && otherName.equals(DOUBLE))) return true;
        
        return false;
    }

    @Override
    public boolean canBeAssigned(Type other) {
        // A basic type can be assigned to the type it can be casted to
        return other.canBeCastedTo(this);
    }
}