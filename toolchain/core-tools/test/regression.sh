mvn clean install --file ../pom.xml

# Imports
cd ./regression/imports
rm a.*
rm file_*.h
rm file_*.c
mvn exec:java -Dexec.args="file_1.escript" --file ../../../pom.xml
clang main.c file_1.c file_2.c
./a
cd ../../

# Global variables
cd ./regression/global_variables
rm a.*
rm *.h
rm variables.c
mvn exec:java -Dexec.args="variables.escript" --file ../../../pom.xml
clang main.c variables.c
./a
cd ../../

# Functions Hello Empty
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="hello_empty.escript" --file ../../../pom.xml
clang hello_empty.c
./a
cd ../../

# Functions Hello Empty With Return type
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="hello_empty_with_rtype.escript" --file ../../../pom.xml
clang hello_empty_with_rtype.c
./a
cd ../../

# Functions typedefs
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="typedef.escript" --file ../../../pom.xml
clang typedef.c
./a
cd ../../

# Functions hello world
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="hello_world.escript" --file ../../../pom.xml
clang hello_world.c
./a
cd ../../

# Functions scan input
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="scan_input.escript" --file ../../../pom.xml
clang scan_input.c
./a <<EOF
James Bond
35
EOF
cd ../../

# Functions first class function
cd ./regression/functions
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="first_class_functions.escript" --file ../../../pom.xml
clang first_class_functions.c
./a
cd ../../

# Control flow
cd ./regression/control_flow
rm a.*
rm *.h
rm *.c
mvn exec:java -Dexec.args="main.escript" --file ../../../pom.xml
clang main.c
./a
echo $?
cd ../../
