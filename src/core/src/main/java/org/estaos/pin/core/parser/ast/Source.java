package org.estaos.pin.core.parser.ast;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.File;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Source {
    private File file;
    private int lineNumber;
    private int startIndex;
    private int endIndex;

    public static Source defaultSource(File file) {
        return new Source(file, -1, 0, -1);
    }
}
