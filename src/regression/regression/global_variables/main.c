#include <stdio.h>
#include "variables.h"

int main() {
    printf("myInt: %d\n", myInt);
    printf("myTypedInt: %d\n", myTypedInt);
    printf("noValueType (uninitialized): %d\n", noValue);
    printf("myNativeString: %s\n", myNativeString);

    printf("addressOfMyInt: %d\n", addressOfMyInt);
    return 0;
}
