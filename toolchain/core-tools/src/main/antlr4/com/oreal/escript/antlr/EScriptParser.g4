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
    | functionCallExpression explicitTypeCastSigil?
    | anonymousFunctionExpression explicitTypeCastSigil?
    ;

primaryExpression
    : numberLiteralExpression
    | charSequenceExpression
    | typePassExpression
    | symbolValueExpression
    | booleanLiteralExpression
    | charLiteralExpression
    | assignmentExpression
    ;

statement
    : functionCallStatement
    | returnStatement
    | variableDeclarationStatement
    | statementsBlock
    | assignmentStatement
    | ifStatement
    ;

ifStatement
    : IF_ OP expression CP statementsBlock
      elseIfBlock*
      elseBlock?
    ;

functionCallStatement
    : functionCallExpression SC
    ;

returnStatement
    : RETURN_ expression SC
    ;

variableDeclarationStatement
    : variableDeclaration
    ;

assignmentStatement
    : assignmentExpression SC
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

symbolValueExpression
    : IDENTIFIER
    ;

booleanLiteralExpression
    : TRUE_ | FALSE_
    ;

charLiteralExpression
    : CHAR
    ;

assignmentExpression
    : variableName EQ (primaryExpression | functionCallExpression)
    ;

functionCallExpression
    : primaryExpression OP functionCallArgumentList? CP
    | functionCallExpression OP functionCallArgumentList? CP
    ;

anonymousFunctionExpression
    : anonymousFunctionDefinition
    | lambdaDefinition
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

anonymousFunctionDefinition
    : anonymousFunctionHeader statementsBlock
    ;

lambdaDefinition
    : anonymousFunctionHeader EG (primaryExpression | functionCallExpression)
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

functionCallArgumentList
    : expression (C expression)*
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
