package com.oreal.escript.codegen.outputs;

public class ClangRuntime {
    public static final String RUNTIME = """
#ifndef ESCRIPT_LANGUAGE_RUNTIME
#define ESCRIPT_LANGUAGE_RUNTIME

#include <stdbool.h>
#include <stdint.h>

typedef int8_t   int8;
typedef int16_t  int16;
typedef int32_t  int32;
typedef int64_t  int64;

typedef int64_t  int128;
typedef int64_t  int256;
typedef int64_t  int512;

#endif // ESCRIPT_LANGUAGE_RUNTIME
""";
}
