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
            String name,
            String documentationMarkdown,
            int dimensions,
            @Nullable TypeReference pointsTo) {
        super(source, name, documentationMarkdown);
        this.dimensions = dimensions;
        this.pointsTo = pointsTo;
    }

    public int getActualDimensions() {
        return dimensions + 1;
    }
}