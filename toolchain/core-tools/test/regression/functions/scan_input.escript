import extern "stdio.h";
import extern "stdlib.h";

function main(): int32 {
    var name = calloc(100 as int64, sizeof(#char));
    printf("Please enter your name: ");
    scanf("%[^\n]s", name);

    var age: int64;
    printf("Please enter your age (years): ");
    scanf("%d", age as any);

    printf("Your name is %s and you are %d years old.", name, age);

    free(name);
}