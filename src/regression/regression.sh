#!/usr/bin/env bash
set -euo pipefail

echo "Building core..."
mvn clean install --file ../core/pom.xml

echo "Building transpiler..."
mvn clean install --file ../transpiler/pom.xml

# -------------------- Imports --------------------
echo -e "\n\n===== Running Imports ====="
cd ./regression/imports
rm -f a.*
rm -f file_*.h
rm -f file_*.c
./../../../transpiler/target/pin "file_1.pin"
clang main.c file_1.c file_2.c
./a
cd ../../

# -------------------- Global Variables --------------------
echo -e "\n\n===== Running Global Variables ====="
cd ./regression/global_variables
rm -f a.*
rm -f *.h
rm -f variables.c
./../../../transpiler/target/pin "variables.pin"
clang main.c variables.c
./a
cd ../../

# -------------------- Functions Hello Empty --------------------
echo -e "\n\n===== Running Functions Hello Empty ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "hello_empty.pin"
clang hello_empty.c
./a
cd ../../

# -------------------- Functions Hello Empty With Return Type --------------------
echo -e "\n\n===== Running Functions Hello Empty With Return Type ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "hello_empty_with_rtype.pin"
clang hello_empty_with_rtype.c
./a
cd ../../

# -------------------- Functions Typedefs --------------------
echo -e "\n\n===== Running Functions Typedefs ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "typedef.pin"
clang typedef.c
./a
cd ../../

# -------------------- Functions Hello World --------------------
echo -e "\n\n===== Running Functions Hello World ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "hello_world.pin"
clang hello_world.c
./a
cd ../../

# -------------------- Functions Scan Input --------------------
echo -e "\n\n===== Running Functions Scan Input ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "scan_input.pin"
clang scan_input.c
./a <<EOF
James Bond
35
EOF
cd ../../

# -------------------- Functions First Class Function --------------------
echo -e "\n\n===== Running Functions First Class Function ====="
cd ./regression/functions
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "first_class_functions.pin"
clang first_class_functions.c
./a
cd ../../

# -------------------- Control Flow --------------------
echo -e "\n\n===== Running Control Flow ====="
cd ./regression/control_flow
rm -f a.*
rm -f *.h
rm -f *.c
./../../../transpiler/target/pin "main.pin"
clang main.c
./a
cd ../../
