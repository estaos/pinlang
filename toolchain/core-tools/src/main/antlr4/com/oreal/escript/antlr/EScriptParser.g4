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
    : VAR_ variableName (CO typeReference)? EQ expression2 SC
    ;

expression2
    : expression2 functionCallArgumentEnclosure
    | typePassExpression
    | anonymousFunctionHeader EG expression2    // lambda
    | anonymousFunctionHeader statementsBlock   // anonymous function
    | expression2 explicitTypeCastSigil
    | NOT expression2
    | SQUIG expression2
    | expression2 ST expression2
    | expression2 SL expression2
    | expression2 PC expression2
    | expression2 PL expression2
    | expression2 MINUS expression2
    | expression2 LTLT expression2
    | expression2 GTGT expression2
    | expression2 LT expression2
    | expression2 LTEQ expression2
    | expression2 GT expression2
    | expression2 GTEQ expression2
    | expression2 EE expression2
    | expression2 NE expression2
    | expression2 A expression2
    | expression2 CIR expression2
    | expression2 P expression2
    | expression2 AA expression2
    | expression2 PP expression2
    | variableName EQ expression2
    | OP expression2 CP // In brackets
    | primaryExpression2
    ;

// Primary expressions are expressions that cannot have other expressions
primaryExpression2
    : numberLiteralExpression
    | charSequenceExpression
    | nullExpression
    | symbolValueExpression
    | booleanLiteralExpression
    | charLiteralExpression
    ;

statement
    : returnStatement
    | variableDeclarationStatement
    | statementsBlock
    | ifStatement
    | continueStatement
    | breakStatement
    | expressionStatement
    ;

ifStatement
    : IF_ OP expression2 CP statementsBlock
      elseIfBlock*
      elseBlock?
    ;

returnStatement
    : RETURN_ expression2 SC
    ;

variableDeclarationStatement
    : variableDeclaration
    ;

continueStatement
    : CONTINUE_
    ;

breakStatement
    : BREAK_
    ;

expressionStatement
    : expression2 SC
    ;

statementsBlock
    : OBC statement* CBC
    ;

elseIfBlock
    : ELSE_ IF_ OP expression2 CP statementsBlock
    ;

elseBlock
    : ELSE_ statementsBlock
    ;

numberLiteralExpression
    : NUMBER
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

typePassExpression
    : HASH IDENTIFIER
    ;

nullExpression
    : NULL_
    ;

symbolValueExpression
    : IDENTIFIER
    ;

booleanLiteralExpression
    : TRUE_ | FALSE_
    ;

charLiteralExpression
    : CHAR
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

anonymousFunctionHeader
    : OP functionParameterList? CP (CO functionReturnType)?
    ;

functionParameterList
    : functionParameter (C functionParameter)* (C functionVarArgsIndicator)?
    | functionVarArgsIndicator
    ;

functionParameter
    : variableName CO typeReference
    ;

functionCallArgumentEnclosure
    : OP (expression2 (C expression2)*)? CP
    ;

functionReturnType
    : typeReference
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

functionVarArgsIndicator
    : DDD
    ;
