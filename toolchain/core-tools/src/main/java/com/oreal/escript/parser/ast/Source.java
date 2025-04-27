package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public class Source {
    private final File source;
    private final int lineNumber;
    private final int columnNumber;
}
