package org.estaos.pin.core.parser.ast;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class Pointer extends BasicType {
    private final int dimensions;

    // If no type is specified, then this is a void pointer.
    private final @Nullable TypeReference pointsTo;

    public Pointer(
            @Nullable Source source,
            String documentationMarkdown,
            int dimensions,
            @Nullable TypeReference pointsTo) {
        super(source, "", documentationMarkdown);
        this.dimensions = dimensions;
        this.pointsTo = pointsTo;
    }

    public int getActualDimensions() {
        return dimensions + 1;
    }

    @Override
    public boolean canBeAssigned(Type other) {
        return other instanceof Pointer otherPointer && otherPointer.canBeCastedTo(this );
    }

    @Override
    public boolean canBeCastedTo(Type other) {
        if (other instanceof BasicType basicType && basicType.getName().equals(BasicType.INT64)) {
            return true;
        }

        if (other instanceof Pointer otherPointer) {
            // Same dimensions and compatible pointed-to types
            if (this.dimensions != otherPointer.dimensions) return false;
            
            // Both void pointers or one is void
            if (this.pointsTo == null || otherPointer.pointsTo == null) return true;
            
            // Check if pointed-to types can be casted
            return this.pointsTo.getType() != null && otherPointer.pointsTo.getType() != null &&
                   this.pointsTo.getType().canBeCastedTo(otherPointer.pointsTo.getType());
        }  else {
            return false;
        }
    }
}