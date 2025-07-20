mvn clean install --file ../pom.xml

# Imports
cd ./regression/imports
rm a.*
rm file_*.h
rm file_*.c
mvn exec:java -Dexec.args="file_1.escript" --file ../../../pom.xml
clang main.c
./a
