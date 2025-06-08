package com.oreal.escript.codegen.outputs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class File {
    /// The path, relative to root, where this file should be saved.
    private final String relativePath;

    private final String contents;
}
