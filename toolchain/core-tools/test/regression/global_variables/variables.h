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
#ifndef VARIABLES_H_
#define VARIABLES_H_
extern int8 myInt;
extern int16 myTypedInt;
extern int16 noValue;
extern char *myNativeString;
extern void* addressOfMyInt;
#endif // VARIABLES_H_
