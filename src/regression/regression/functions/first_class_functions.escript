import extern "stdio.h";

typedef function NumberSupplier(): double;
typedef function NoArgsCallable(): double;
typedef function CallableWithArg(arg: double): double;

function getNumber(): double {
    return 45;
}

function getNumberSupplier(): NumberSupplier {
    return getNumber;
}

function callSupplier(supplier: NumberSupplier): double {
    return supplier();
}

function callNoArgsCallable(callable: NoArgsCallable): double {
    return callable();
}

function callCallableWithArg(arg: double, callable: CallableWithArg): double {
    return callable(arg);
}

function main(): int32 {
    var supplier = getNumberSupplier();
    printf("Supplier returned %f\n", callSupplier(supplier));
    printf("No args callable returned %f\n", callNoArgsCallable(supplier));
    printf("Lambda returned %f\n", callNoArgsCallable((): double => 64));
    printf("Anonymouse function returned %f\n", callNoArgsCallable((): double {
        return 128;
    }));

    var someInt: double = 256;
    printf("Callable with arg passed %f and returned %f\n", someInt, callCallableWithArg(someInt, (arg: double): double => arg));

    supplier = (): double => 512;
    printf("Lambda Assigned returned %f\n", callNoArgsCallable(supplier));

    return 0;
}
