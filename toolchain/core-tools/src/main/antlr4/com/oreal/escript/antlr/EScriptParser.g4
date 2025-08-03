parser grammar EScriptParser;

options {
    tokenVocab = EScriptLexer;
}

compilationUnit
    : (languageImport
    | externalImport
    | variableDeclaration
    | functionDefinition
    | functionTypeDef)* EOF
    ;

languageImport
    : IMPORT_ importPath SC
    ;

externalImport
    : IMPORT_ EXTERN_ importPath SC
    ;

variableDeclaration
    : documentationCommentLines? (variableDeclarationWithNoInitialisation | variableDeclarationWithInitialisation)
    ;

variableDeclarationWithNoInitialisation
    : VAR_ variableName CO nonArrayTypeReference SC
    | VAR_ variableName CO arrayTypeReference SC
    ;

variableDeclarationWithInitialisation
    : VAR_ variableName (CO typeReference)? EQ expression SC
    ;

expression
// TODO: Come back to add literals and all other types of expressions. Also includes array expressions.
    : primaryExpression explicitTypeCastSigil?
    ;

primaryExpression
    : numberLiteralExpression
    | charSequenceExpression
    | symbolValueExpression
    ;

numberLiteralExpression
    : NUMBER
    ;

symbolValueExpression
    : IDENTIFIER
    ;

charSequenceExpression
    : singleLineCharSequenceExpression
    | multilineCharSequenceExpression
    ;

singleLineCharSequenceExpression
    : SINGLE_LINE_STRING
    ;

multilineCharSequenceExpression
    : MULTI_LINE_STRING
    ;

explicitTypeCastSigil
    : AS_ typeReference
    ;

variableName
    : IDENTIFIER
    ;

typeReference
    : nonArrayTypeReference
    | arrayTypeReference
    ;

nonArrayTypeReference
    : IDENTIFIER
    ;

arrayTypeReference
    : IDENTIFIER arrayIndexingWithOptionalIndex+
    ;

functionTypeDef
    : TYPEDEF_ functionHeader SC
    ;

functionDefinition
    : functionHeader statementsBlock
    ;

functionHeader
    : FUNCTION_ variableName OP functionParameterList? CP (CO functionReturnType)?
    ;

functionParameterList
    : functionParameter (C functionParameter)*
    ;

functionParameter
    : variableName CO typeReference
    ;

functionReturnType
    : typeReference
    ;

statementsBlock
// TODO: Add statements to function
    : OBC CBC
    ;

arrayIndexingWithOptionalIndex
    : (OB CB)
    // TODO: Add optional expression once AST supports it
//    : (OB expression? CB)
    ;

importPath
    : SINGLE_LINE_STRING
    ;

documentationCommentLines
    : MULTI_LINE_COMMENT_LINE+
    ;