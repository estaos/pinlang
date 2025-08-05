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

    var a = 5;
    var b = 512;

    if(a > b) {
        // Should never happen
        assert(0);
    }

    if(a == 5 && b == 512) {
        printf("a==5 and b==512\n");
    }

    var counter = 10;
    while(counter > 0) {
        counter = counter - 1;
    }
    assert(counter == 0);

    counter = 10;
    do {
        counter = counter - 1;
    } while(counter > 0);
    assert(counter == 0);
    
    counter = 10;
    for(var iii = 0; iii < 10; iii = iii+1) {
        counter = counter -1;
    }
    assert(counter == 0);

    counter = 10;
    for(counter > 0; counter = counter - 1) {}
    assert(counter == 0);

    return 0;
}
