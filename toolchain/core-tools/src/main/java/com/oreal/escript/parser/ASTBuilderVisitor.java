package com.oreal.escript.parser;

import com.oreal.escript.antlr.*;
import com.oreal.escript.parser.ast.AddExpression;
import com.oreal.escript.parser.ast.Argument;
import com.oreal.escript.parser.ast.AssignmentExpression;
import com.oreal.escript.parser.ast.BinaryOperatorExpression;
import com.oreal.escript.parser.ast.BitwiseAndExpression;
import com.oreal.escript.parser.ast.BitwiseLeftShift;
import com.oreal.escript.parser.ast.BitwiseNotExpression;
import com.oreal.escript.parser.ast.BitwiseOrExpression;
import com.oreal.escript.parser.ast.BitwiseRightShift;
import com.oreal.escript.parser.ast.BitwiseXorExpression;
import com.oreal.escript.parser.ast.BlockExpression;
import com.oreal.escript.parser.ast.BooleanLiteralExpression;
import com.oreal.escript.parser.ast.BreakStatement;
import com.oreal.escript.parser.ast.CallableCode;
import com.oreal.escript.parser.ast.CallableCodeExpression;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CharLiteralExpression;
import com.oreal.escript.parser.ast.CharSequenceLiteralExpression;
import com.oreal.escript.parser.ast.CompareEqualToExpression;
import com.oreal.escript.parser.ast.CompareGreaterThanEqualToExpression;
import com.oreal.escript.parser.ast.CompareGreaterThanExpression;
import com.oreal.escript.parser.ast.CompareLessThanEqualToExpression;
import com.oreal.escript.parser.ast.CompareLessThanExpression;
import com.oreal.escript.parser.ast.CompareNotEqualToExpression;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.ContinueStatement;
import com.oreal.escript.parser.ast.DivisionExpression;
import com.oreal.escript.parser.ast.ExplicitCastExpression;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.FunctionCallExpression;
import com.oreal.escript.parser.ast.IfStatement;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.LogicalAndExpression;
import com.oreal.escript.parser.ast.LogicalNotExpression;
import com.oreal.escript.parser.ast.LogicalOrExpression;
import com.oreal.escript.parser.ast.ModulusExpression;
import com.oreal.escript.parser.ast.MultiplyExpression;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NullExpression;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.ReturnStatement;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.SubtractExpression;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.SymbolValueExpression;
import com.oreal.escript.parser.ast.Type;
import com.oreal.escript.parser.ast.TypePassExpression;
import com.oreal.escript.parser.ast.TypeReference;
import com.oreal.escript.parser.ast.VariableDeclaration;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ASTBuilderVisitor implements EScriptParserVisitor<Object> {
    /// The file being parsed
    private final File file;
    private final List<FunctionDefinition> pendingFunctionDefinitions = new LinkedList<>();

    public static final String CALLABLE_TYPE_SIGIL = "_type__";
    public static final String CALLABLE_CODE_SIGIL = "_code__";

    public ASTBuilderVisitor(File file) {
        this.file = file;
    }

    @Override
    public CompilationUnit visitCompilationUnit(EScriptParser.CompilationUnitContext ctx) {
        List<Import> imports = new LinkedList<>();
        imports.addAll(ctx.languageImport().stream().map(this::visitLanguageImport).toList());
        imports.addAll(ctx.externalImport().stream().map(this::visitExternalImport).toList());

        List<NamedValueSymbol> symbols = new LinkedList<>(ctx.variableDeclaration().stream().map(this::visitVariableDeclaration).toList());
        List<Type> types = new LinkedList<>(ctx.functionTypeDef().stream().map(this::visitFunctionTypeDef).toList());
        List<CallableCode> callableCodeBlocks = new LinkedList<>();

        List<FunctionDefinition> functionDefinitions = new LinkedList<>();
        functionDefinitions.addAll(ctx.functionDefinition().stream().map(this::visitFunctionDefinition).toList());
        functionDefinitions.addAll(pendingFunctionDefinitions);

        for(FunctionDefinition functionDefinition : functionDefinitions) {
            types.add(functionDefinition.callableType);
            callableCodeBlocks.add(functionDefinition.callableCode);

            if(functionDefinition.symbol() != null) {
                symbols.add(functionDefinition.symbol);
            }
        }

        return new CompilationUnit(file, imports, symbols, types, callableCodeBlocks);
    }

    @Override
    public Import visitLanguageImport(EScriptParser.LanguageImportContext ctx) {
        File importFile = getImportFile(ctx.importPath());
        Source source = getNodeSource(file, ctx);
        return new Import("", false, source, importFile, null);
    }

    @Override
    public Import visitExternalImport(EScriptParser.ExternalImportContext ctx) {
        File importFile = getImportFile(ctx.importPath());
        Source source = getNodeSource(file, ctx);
        return new Import("", true, source, importFile, null);
    }

    @Override
    public NamedValueSymbol visitVariableDeclaration(EScriptParser.VariableDeclarationContext ctx) {
        String documentation = Optional.ofNullable(ctx.documentationCommentLines())
                .map(this::visitDocumentationCommentLines)
                .orElse("");

        NamedValueSymbol symbol = Optional.ofNullable(ctx.variableDeclarationWithInitialisation())
                .map(this::visitVariableDeclarationWithInitialisation)
                .orElseGet(() -> visitVariableDeclarationWithNoInitialisation(ctx.variableDeclarationWithNoInitialisation()));

        symbol.setDocumentationMarkdown(documentation);
        return symbol;
    }

    @Override
    public NamedValueSymbol visitVariableDeclarationWithNoInitialisation(EScriptParser.VariableDeclarationWithNoInitialisationContext ctx) {
        String variableName = visitVariableName(ctx.variableName());
        TypeReference typeReference = Optional.ofNullable(ctx.nonArrayTypeReference())
                .map(this::visitNonArrayTypeReference)
                .orElseGet(() -> visitArrayTypeReference(ctx.arrayTypeReference()));

        return new NamedValueSymbol(
                variableName, typeReference,
                getNodeSource(file, ctx), true, false,
                "", null, false);
    }

    @Override
    public NamedValueSymbol visitVariableDeclarationWithInitialisation(EScriptParser.VariableDeclarationWithInitialisationContext ctx) {
        String variableName = visitVariableName(ctx.variableName());
        @Nullable TypeReference typeReference = Optional.ofNullable(ctx.typeReference())
                .map(this::visitTypeReference)
                .orElse(null);

        Expression value = visitExpression2(ctx.expression2());

        return new NamedValueSymbol(
                variableName, typeReference,
                getNodeSource(file, ctx), true, false,
                "", value, false);
    }

    @Override
    public Expression visitExpression2(EScriptParser.Expression2Context ctx) {
        if(ctx.primaryExpression2() != null) {
            return visitPrimaryExpression2(ctx.primaryExpression2());
        } else if(ctx.typePassExpression() != null) {
            return visitTypePassExpression(ctx.typePassExpression());
        } else if(ctx.statementsBlock() != null && ctx.anonymousFunctionHeader() != null) {
            // Anonymous function
            FunctionDefinition functionDefinition = getAnonymousFunction(ctx.anonymousFunctionHeader(), ctx.statementsBlock());
            pendingFunctionDefinitions.add(functionDefinition);
            return new SymbolValueExpression(getNodeSource(file, ctx), Objects.requireNonNull(functionDefinition.symbol()).getName());
        } else if(!ctx.expression2().isEmpty()) {
            Expression left = visitExpression2(ctx.expression2().getFirst());
            if(ctx.expression2().size() > 1) {
                Expression right = visitExpression2(ctx.expression2().get(1));
                return getBinaryExpression(ctx, left, right);
            } else if(ctx.EG() != null) {
                // lambda
                FunctionDefinition functionDefinition = getLambda(ctx.anonymousFunctionHeader(), left);
                pendingFunctionDefinitions.add(functionDefinition);
                return new SymbolValueExpression(getNodeSource(file, ctx), Objects.requireNonNull(functionDefinition.symbol()).getName());
            } else if(ctx.functionCallArgumentEnclosure() != null) {
                // Function call
                return getFunctionCall(ctx, left, ctx.functionCallArgumentEnclosure());
            } else if(ctx.OP() != null && ctx.CP() != null) {
                // Brackets
                return left;
            } else {
                return getUnaryExpression(left, ctx);
            }
        } else {
            throw new UnsupportedOperationException("Cannot parse expression " + ctx);
        }
    }

    @Override
    public Expression visitPrimaryExpression2(EScriptParser.PrimaryExpression2Context ctx) {
        if(ctx.numberLiteralExpression() != null) {
            return visitNumberLiteralExpression(ctx.numberLiteralExpression());
        } else if(ctx.charSequenceExpression() != null) {
            return visitCharSequenceExpression(ctx.charSequenceExpression());
        } else if(ctx.symbolValueExpression() != null) {
            return visitSymbolValueExpression(ctx.symbolValueExpression());
        } else if(ctx.booleanLiteralExpression() != null) {
            return visitBooleanLiteralExpression(ctx.booleanLiteralExpression());
        } else if(ctx.charLiteralExpression() != null) {
            return visitCharLiteralExpression(ctx.charLiteralExpression());
        } else if(ctx.nullExpression() != null) {
            return visitNullExpression(ctx.nullExpression());
        } else {
            throw new IllegalArgumentException("Unknown expression " + ctx);
        }
    }

    @Override
    public Expression visitStatement(EScriptParser.StatementContext ctx) {
        if(ctx.returnStatement() != null) {
            return visitReturnStatement(ctx.returnStatement());
        } else if(ctx.variableDeclarationStatement() != null) {
            return visitVariableDeclarationStatement(ctx.variableDeclarationStatement());
        } else if(ctx.statementsBlock() != null) {
            return visitStatementsBlock(ctx.statementsBlock());
        } else if(ctx.ifStatement() != null) {
            return visitIfStatement(ctx.ifStatement());
        } else if(ctx.continueStatement() != null) {
            return visitContinueStatement(ctx.continueStatement());
        } else if(ctx.breakStatement() != null) {
            return visitBreakStatement(ctx.breakStatement());
        } else if(ctx.expressionStatement() != null) {
            return visitExpressionStatement(ctx.expressionStatement());
        } else {
            throw new IllegalArgumentException("Unknown statement " + ctx);
        }
    }

    @Override
    public IfStatement visitIfStatement(EScriptParser.IfStatementContext ctx) {
        Expression expression = visitExpression2(ctx.expression2());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());

        @Nullable BlockExpression elseBlock = Optional.ofNullable(ctx.elseBlock())
                .map(this::visitElseBlock)
                .orElse(null);

        List<IfStatement.ElseIfBlock> elseIfBlocks = ctx.elseIfBlock().stream().map(this::visitElseIfBlock).toList();

        return new IfStatement(getNodeSource(file, ctx), expression, blockExpression, elseBlock, elseIfBlocks);
    }

    @Override
    public ReturnStatement visitReturnStatement(EScriptParser.ReturnStatementContext ctx) {
        return new ReturnStatement(getNodeSource(file, ctx), visitExpression2(ctx.expression2()));
    }

    @Override
    public VariableDeclaration visitVariableDeclarationStatement(EScriptParser.VariableDeclarationStatementContext ctx) {
        return new VariableDeclaration(
                getNodeSource(file, ctx),
                visitVariableDeclaration(ctx.variableDeclaration()));
    }

    @Override
    public ContinueStatement visitContinueStatement(EScriptParser.ContinueStatementContext ctx) {
        return new ContinueStatement(getNodeSource(file, ctx));
    }

    @Override
    public BreakStatement visitBreakStatement(EScriptParser.BreakStatementContext ctx) {
        return new BreakStatement(getNodeSource(file, ctx));
    }

    @Override
    public Expression visitExpressionStatement(EScriptParser.ExpressionStatementContext ctx) {
        return visitExpression2(ctx.expression2());
    }

    @Override
    public NumberLiteralExpression visitNumberLiteralExpression(EScriptParser.NumberLiteralExpressionContext ctx) {
        return new NumberLiteralExpression(getNodeSource(file, ctx), ctx.NUMBER().getText());
    }

    @Override
    public SymbolValueExpression visitSymbolValueExpression(EScriptParser.SymbolValueExpressionContext ctx) {
        return new SymbolValueExpression(getNodeSource(file, ctx), ctx.IDENTIFIER().getText());
    }

    @Override
    public CharSequenceLiteralExpression visitCharSequenceExpression(EScriptParser.CharSequenceExpressionContext ctx) {
        if(ctx.singleLineCharSequenceExpression() != null) {
            return visitSingleLineCharSequenceExpression(ctx.singleLineCharSequenceExpression());
        } else {
            return visitMultilineCharSequenceExpression(ctx.multilineCharSequenceExpression());
        }
    }

    @Override
    public CharSequenceLiteralExpression visitSingleLineCharSequenceExpression(EScriptParser.SingleLineCharSequenceExpressionContext ctx) {
        return new CharSequenceLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.SINGLE_LINE_STRING().getText()));
    }

    @Override
    public CharSequenceLiteralExpression visitMultilineCharSequenceExpression(EScriptParser.MultilineCharSequenceExpressionContext ctx) {
        return new CharSequenceLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.MULTI_LINE_STRING().getText()));
    }

    @Override
    public TypePassExpression visitTypePassExpression(EScriptParser.TypePassExpressionContext ctx) {
        return new TypePassExpression(
                getNodeSource(file, ctx), new TypeReference(
                ctx.IDENTIFIER().getText(),
                null,0, List.of()
        ));
    }

    @Override
    public NullExpression visitNullExpression(EScriptParser.NullExpressionContext ctx) {
        return new NullExpression(getNodeSource(file, ctx));
    }

    @Override
    public BooleanLiteralExpression visitBooleanLiteralExpression(EScriptParser.BooleanLiteralExpressionContext ctx) {
        return new BooleanLiteralExpression(getNodeSource(file, ctx), ctx.FALSE_() == null);
    }

    @Override
    public CharLiteralExpression visitCharLiteralExpression(EScriptParser.CharLiteralExpressionContext ctx) {
        return new CharLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.CHAR().getText()).charAt(0));
    }

    @Override
    public TypeReference visitExplicitTypeCastSigil(EScriptParser.ExplicitTypeCastSigilContext ctx) {
        return visitTypeReference(ctx.typeReference());
    }

    @Override
    public String visitVariableName(EScriptParser.VariableNameContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public TypeReference visitTypeReference(EScriptParser.TypeReferenceContext ctx) {
        if(ctx.nonArrayTypeReference() != null) {
            return visitNonArrayTypeReference(ctx.nonArrayTypeReference());
        } else {
            return visitArrayTypeReference(ctx.arrayTypeReference());
        }
    }

    @Override
    public TypeReference visitNonArrayTypeReference(EScriptParser.NonArrayTypeReferenceContext ctx) {
        return new TypeReference(ctx.IDENTIFIER().getText(), null, 0, List.of());
    }

    @Override
    public TypeReference visitArrayTypeReference(EScriptParser.ArrayTypeReferenceContext ctx) {
        int dimensionsCount = ctx.arrayIndexingWithOptionalIndex().size();
        return new TypeReference(ctx.IDENTIFIER().getText(), null, dimensionsCount, List.of());
    }

    @Override
    public CallableType visitFunctionTypeDef(EScriptParser.FunctionTypeDefContext ctx) {
        String typeName = visitVariableName(ctx.functionHeader().variableName());
        CallableType callableType = visitFunctionHeader(ctx.functionHeader());
        callableType.setName(typeName);
        return callableType;
    }

    @Override
    public FunctionDefinition visitFunctionDefinition(EScriptParser.FunctionDefinitionContext ctx) {
        String symbolName = visitVariableName(ctx.functionHeader().variableName());
        CallableType callableType = visitFunctionHeader(ctx.functionHeader());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());
        Source source = getNodeSource(file, ctx);

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    @Override
    public CallableType visitFunctionHeader(EScriptParser.FunctionHeaderContext ctx) {
        Source source = getNodeSource(file, ctx);
        boolean isVarArgs = false;
        List<NamedValueSymbol> parameters = List.of();
        if(ctx.functionParameterList() != null) {
            parameters = visitFunctionParameterList(ctx.functionParameterList());
            isVarArgs = ctx.functionParameterList().functionVarArgsIndicator() != null;
        }

        @Nullable TypeReference returnType = Optional.ofNullable(ctx.functionReturnType())
                .map(this::visitFunctionReturnType)
                .orElse(null);

        return new CallableType(source, "", List.of(), "", parameters, returnType, isVarArgs);
    }

    @Override
    public CallableType visitAnonymousFunctionHeader(EScriptParser.AnonymousFunctionHeaderContext ctx) {
        Source source = getNodeSource(file, ctx);
        boolean isVarArgs = false;
        List<NamedValueSymbol> parameters = List.of();
        if(ctx.functionParameterList() != null) {
            parameters = visitFunctionParameterList(ctx.functionParameterList());
            isVarArgs = ctx.functionParameterList().functionVarArgsIndicator() != null;
        }

        @Nullable TypeReference returnType = Optional.ofNullable(ctx.functionReturnType())
                .map(this::visitFunctionReturnType)
                .orElse(null);

        return new CallableType(source, "", List.of(), "", parameters, returnType, isVarArgs);
    }

    @Override
    public List<NamedValueSymbol> visitFunctionParameterList(EScriptParser.FunctionParameterListContext ctx) {
        return ctx.functionParameter().stream().map(this::visitFunctionParameter).toList();
    }

    @Override
    public NamedValueSymbol visitFunctionParameter(EScriptParser.FunctionParameterContext ctx) {
        return new NamedValueSymbol(
                ctx.variableName().getText(),
                visitTypeReference(ctx.typeReference()),
                getNodeSource(file, ctx),
                true,
                false,
                "",
                null,
                false
        );
    }

    @Override
    public List<Argument> visitFunctionCallArgumentEnclosure(EScriptParser.FunctionCallArgumentEnclosureContext ctx) {
        return ctx.expression2().stream().map(this::visitExpression2).map(Argument::new).toList();
    }

    @Override
    public TypeReference visitFunctionReturnType(EScriptParser.FunctionReturnTypeContext ctx) {
        return visitTypeReference(ctx.typeReference());
    }

    @Override
    public BlockExpression visitStatementsBlock(EScriptParser.StatementsBlockContext ctx) {
        return new BlockExpression(getNodeSource(file, ctx), ctx.statement().stream().map(this::visitStatement).toList());
    }

    @Override
    public IfStatement.ElseIfBlock visitElseIfBlock(EScriptParser.ElseIfBlockContext ctx) {
        Expression expression = visitExpression2(ctx.expression2());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());

        return new IfStatement.ElseIfBlock(expression, blockExpression);
    }

    @Override
    public BlockExpression visitElseBlock(EScriptParser.ElseBlockContext ctx) {
        return visitStatementsBlock(ctx.statementsBlock());
    }

    @Override
    public Object visitArrayIndexingWithOptionalIndex(EScriptParser.ArrayIndexingWithOptionalIndexContext ctx) {
        return null;
    }

    @Override
    public String visitImportPath(EScriptParser.ImportPathContext ctx) {
        return unquoteString(ctx.getText());
    }

    @Override
    public String visitDocumentationCommentLines(EScriptParser.DocumentationCommentLinesContext ctx) {
        return ctx.MULTI_LINE_COMMENT_LINE().stream().map(ParseTree::getText).collect(Collectors.joining("\n"));
    }

    @Override
    public Object visitFunctionVarArgsIndicator(EScriptParser.FunctionVarArgsIndicatorContext ctx) {
        return null;
    }

    @Override
    public CompilationUnit visit(ParseTree parseTree) {
        return visitCompilationUnit((EScriptParser.CompilationUnitContext) parseTree);
    }

    @Override
    public Object visitChildren(RuleNode ruleNode) {
        return null;
    }

    @Override
    public Object visitTerminal(TerminalNode terminalNode) {
        return null;
    }

    @Override
    public Object visitErrorNode(ErrorNode errorNode) {
        return null;
    }

    private Source getNodeSource(File file, ParserRuleContext ctx) {
        return new Source(file, ctx.start.getLine(), ctx.getStart().getStartIndex(), ctx.stop.getStopIndex());
    }

    private String unquoteString(String singleLineStringWithQuotes) {
        return singleLineStringWithQuotes.substring(1, singleLineStringWithQuotes.length()-1);
    }

    private File getImportFile(EScriptParser.ImportPathContext importPathCtx) {
        String parent = Optional.ofNullable(Path.of(file.getPath()).getParent())
                .map(Path::toString)
                .orElse("");

        Path importPath = Path.of(parent, visitImportPath(importPathCtx));
        return importPath.toFile();
    }

    private FunctionDefinition getFunctionDefinition(Source source, String symbolName, CallableType callableType, BlockExpression blockExpression) {
        String typeName = String.format("%s_%s", symbolName, CALLABLE_TYPE_SIGIL);
        callableType.setName(typeName);

        boolean isMain = symbolName.equals("main");
        String callableBlockName = isMain ? symbolName : String.format("%s_%s", symbolName, CALLABLE_CODE_SIGIL);
        CallableCode callableCode = new CallableCode(
                callableBlockName,
                callableType.getSource(), callableType, blockExpression);

        NamedValueSymbol symbol = new NamedValueSymbol(
                symbolName,
                TypeReference.ofType(callableType),
                callableCode.getSource(),
                true, false, "",
                new CallableCodeExpression(source, callableCode), false
        );

        if(isMain) {
            return new FunctionDefinition(callableType, callableCode, null);
        } else {
            return new FunctionDefinition(callableType, callableCode, symbol);
        }
    }

    private String getRandomSymbolName() {
        return String.format("temp_%s", UUID.randomUUID().toString().replaceAll("-", "_"));
    }

    private Expression getBinaryExpression(EScriptParser.Expression2Context ctx, Expression left, Expression right) {
        BinaryOperatorExpression expression;

        if (ctx.ST() != null) {
            expression = new MultiplyExpression(left, right);
        } else if (ctx.SL() != null) {
            expression = new DivisionExpression(left, right);
        } else if (ctx.PC() != null) {
            expression = new ModulusExpression(left, right);
        } else if (ctx.PL() != null) {
            expression = new AddExpression(left, right);
        } else if (ctx.MINUS() != null) {
            expression = new SubtractExpression(left, right);
        } else if (ctx.LTLT() != null) {
            expression = new BitwiseLeftShift(left, right);
        } else if (ctx.GTGT() != null) {
            expression = new BitwiseRightShift(left, right);
        } else if (ctx.LT() != null) {
            expression = new CompareLessThanExpression(left, right);
        } else if (ctx.LTEQ() != null) {
            expression = new CompareLessThanEqualToExpression(left, right);
        } else if (ctx.GT() != null) {
            expression = new CompareGreaterThanExpression(left, right);
        } else if (ctx.GTEQ() != null) {
            expression = new CompareGreaterThanEqualToExpression(left, right);
        } else if (ctx.EE() != null) {
            expression = new CompareEqualToExpression(left, right);
        } else if (ctx.NE() != null) {
            expression = new CompareNotEqualToExpression(left, right);
        } else if (ctx.A() != null) {
            expression = new BitwiseAndExpression(left, right);
        } else if (ctx.CIR() != null) {
            expression = new BitwiseXorExpression(left, right);
        } else if (ctx.P() != null) {
            expression = new BitwiseOrExpression(left, right);
        } else if (ctx.AA() != null) {
            expression = new LogicalAndExpression(left, right);
        } else if(ctx.PP() != null) {
            expression = new LogicalOrExpression(left, right);
        } else {
            throw new UnsupportedOperationException("Cannot parse binary operator " + ctx);
        }

        expression.setSource(getNodeSource(file, ctx));
        return expression;
    }

    private Expression getUnaryExpression(Expression operand, EScriptParser.Expression2Context ctx) {
        if(ctx.NOT() != null) {
            return new LogicalNotExpression(getNodeSource(file, ctx), operand);
        } else if(ctx.SQUIG() != null) {
            return new BitwiseNotExpression(getNodeSource(file, ctx), operand);
        } else if(ctx.EQ() != null) {
            return new AssignmentExpression(getNodeSource(file, ctx), visitVariableName(ctx.variableName()), operand);
        } else if(ctx.explicitTypeCastSigil() != null) {
            return getExplicitCast(operand, ctx.explicitTypeCastSigil());
        } else {
            throw new UnsupportedOperationException("Cannot parse operator yet " + ctx);
        }
    }

    private ExplicitCastExpression getExplicitCast(Expression operand, EScriptParser.ExplicitTypeCastSigilContext ctx) {
        return new ExplicitCastExpression(getNodeSource(file, ctx), operand, visitExplicitTypeCastSigil(ctx));
    }

    private FunctionDefinition getAnonymousFunction(EScriptParser.AnonymousFunctionHeaderContext headerContext, EScriptParser.StatementsBlockContext statementsBlockContext) {
        BlockExpression blockExpression = visitStatementsBlock(statementsBlockContext);
        CallableType callableType = visitAnonymousFunctionHeader(headerContext);
        String symbolName = getRandomSymbolName();
        Source source = getNodeSource(file, headerContext);

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    private FunctionDefinition getLambda(EScriptParser.AnonymousFunctionHeaderContext headerContext, Expression returnValue) {
        CallableType callableType = visitAnonymousFunctionHeader(headerContext);
        String symbolName = getRandomSymbolName();
        Source source = getNodeSource(file, headerContext);

        BlockExpression blockExpression = new BlockExpression(source, List.of(
                new ReturnStatement(source, returnValue)
        ));

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    private FunctionCallExpression getFunctionCall(ParserRuleContext ctx, Expression callableExpression, EScriptParser.FunctionCallArgumentEnclosureContext functionCallArgumentEnclosureContext) {
        List<Argument> arguments = visitFunctionCallArgumentEnclosure(functionCallArgumentEnclosureContext);
        return new FunctionCallExpression(getNodeSource(file, ctx), arguments, callableExpression);
    }

    public record FunctionDefinition(CallableType callableType, CallableCode callableCode, @Nullable NamedValueSymbol symbol) {}
}
