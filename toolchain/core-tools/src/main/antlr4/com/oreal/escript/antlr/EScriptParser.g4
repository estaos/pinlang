parser grammar EScriptParser;

options {
    tokenVocab = EScriptLexer;
}

compilationUnit
    : (languageImport | externalImport | variableDeclaration)* EOF
    ;

languageImport
    : IMPORT_ importPath SC
    ;

externalImport
    : IMPORT_ EXTERN_ importPath SC
    ;

variableDeclaration
    : variableDeclarationWithNoInitialisation
    | variableDeclarationWithInitialisation
    ;

variableDeclarationWithNoInitialisation
    : VAR_ variableName CO nonArrayTypeReference SC
    | VAR_ variableName CO arrayTypeReference SC
    ;

variableDeclarationWithInitialisation
    : VAR_ variableName (CO nonArrayTypeReference)? EQ expression SC
    | VAR_ variableName (CO arrayTypeReference)? EQ expression SC
    ;

expression
// TODO: Come back to add literals and all other types of expressions. Also includes array expressions.
    : NUMBER
    ;

variableName
    : IDENTIFIER
    ;

nonArrayTypeReference
    : IDENTIFIER
    ;

arrayTypeReference
    : IDENTIFIER + arrayIndexingWithOptionalIndex+
    ;

arrayIndexingWithOptionalIndex
    : (OB expression? CB)
    ;

importPath
    : SINGLE_LINE_STRING
    ;
