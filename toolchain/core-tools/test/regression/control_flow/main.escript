import extern "assert.h";
import extern "stdio.h";

function main(): int32 {
    if(true) {
        printf("Hello from plain if statement\n");
    }

    if(false) {
        // Should not execute
        assert(0);
    } else {
        printf("Hello from else block in if else\n");
    }

    if(false) {
        // Should not execute
        assert(0);
    } else if(true) {
        printf("Hello from else if block in if if-else else\n");
    } else {
        // Should not execute
        assert(0);
    }
    
    return 0;
}
