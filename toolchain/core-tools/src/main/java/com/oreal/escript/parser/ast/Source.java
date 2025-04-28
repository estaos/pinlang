package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public class Source {
    private File file;
    private int lineNumber;
    private int startColumn;
    private int endColumn;

    public static Source defaultSource(File file) {
        return new Source(file, -1, 0, -1);
    }
}
