package com.oreal.escript.parser;

import java.io.File;

public interface ImportFileResolver {
    File resolve(String path, File importedFrom);
}
