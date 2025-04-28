package com.oreal.escript.parser.ast;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public class Source {
    private File source;
    private int lineNumber;
    private int columnNumber;
}
