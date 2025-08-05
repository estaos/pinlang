import extern "stdio.h";

typedef function NumberSupplier(): double;
typedef function NoArgsCallable(): double;

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

function main() {
    var supplier = getNumberSupplier();
    printf("Supplier returned %f\n", callSupplier(supplier));
    printf("No args callable returned %f", callNoArgsCallable(supplier));
}
