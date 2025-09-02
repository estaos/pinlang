parser grammar PinLangParser;

options {
    tokenVocab = PinLangLexer;
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
    : expression functionCallArgumentEnclosure
    | typePassExpression
    | anonymousFunctionHeader EG expression    // lambda
    | anonymousFunctionHeader statementsBlock   // anonymous function
    | expression explicitTypeCastSigil
    | NOT expression
    | SQUIG expression
    | expression ST expression
    | expression SL expression
    | expression PC expression
    | expression PL expression
    | expression MINUS expression
    | expression LTLT expression
    | expression GTGT expression
    | expression LT expression
    | expression LTEQ expression
    | expression GT expression
    | expression GTEQ expression
    | expression EE expression
    | expression NE expression
    | expression A expression
    | expression CIR expression
    | expression P expression
    | expression AA expression
    | expression PP expression
    | variableName EQ expression
    | OP expression CP // In brackets
    | primaryExpression
    ;

// Primary expressions are expressions that cannot have other expressions
primaryExpression
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
    | whileLoopStatement
    | doWhileLoopStatement
    | forLoopStatement
    | expressionStatement
    ;

ifStatement
    : IF_ OP expression CP statementsBlock
      elseIfBlock*
      elseBlock?
    ;

whileLoopStatement
    : WHILE_ OP expression CP statementsBlock
    ;

doWhileLoopStatement
    : DO_ statementsBlock WHILE_ OP expression CP SC
    ;

forLoopStatement
    : FOR_ OP variableDeclaration? expression SC expression CP statementsBlock
    ;

returnStatement
    : RETURN_ expression SC
    ;

variableDeclarationStatement
    : variableDeclaration
    ;

continueStatement
    : CONTINUE_ SC
    ;

breakStatement
    : BREAK_ SC
    ;

expressionStatement
    : expression SC
    ;

statementsBlock
    : OBC statement* CBC
    ;

elseIfBlock
    : ELSE_ IF_ OP expression CP statementsBlock
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
    : OP (expression (C expression)*)? CP
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
