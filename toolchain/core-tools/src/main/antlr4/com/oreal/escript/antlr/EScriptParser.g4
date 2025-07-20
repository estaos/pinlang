parser grammar EScriptParser;

options {
    tokenVocab = EScriptLexer;
}

compilationUnit
    : (languageImport | externalImport)* EOF
    ;

languageImport
    : IMPORT_ SINGLE_LINE_STRING SC
    ;

externalImport
    : IMPORT_ EXTERN_ SINGLE_LINE_STRING SC
    ;
