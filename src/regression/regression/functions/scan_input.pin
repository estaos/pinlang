import extern "stdio.h";
import extern "stdlib.h";

var NAME_BUFFER_SIZE: int64 = 100;

function main(): int32 {
    var namePtr = calloc(NAME_BUFFER_SIZE, sizeof(#char));
    var name = namePtr as char[];

    printf("Please enter your name: ");
    scanf_s("%[^\n]s", name);

    var age: int64;
    printf("Please enter your age (years): ");
    scanf_s("%lld", age as any);

    printf("Your name is %s and you are %lld years old.", name, age);

    free(namePtr);
    return 0;
}