package org.estaos.pin.core.parser;

import org.estaos.pin.antlr.PinLangParser;
import org.estaos.pin.antlr.PinLangParserVisitor;
import org.estaos.pin.core.parser.ast.AddExpression;
import org.estaos.pin.core.parser.ast.Argument;
import org.estaos.pin.core.parser.ast.AssignmentExpression;
import org.estaos.pin.core.parser.ast.BinaryOperatorExpression;
import org.estaos.pin.core.parser.ast.BitwiseAndExpression;
import org.estaos.pin.core.parser.ast.BitwiseLeftShift;
import org.estaos.pin.core.parser.ast.BitwiseNotExpression;
import org.estaos.pin.core.parser.ast.BitwiseOrExpression;
import org.estaos.pin.core.parser.ast.BitwiseRightShift;
import org.estaos.pin.core.parser.ast.BitwiseXorExpression;
import org.estaos.pin.core.parser.ast.BlockExpression;
import org.estaos.pin.core.parser.ast.BooleanLiteralExpression;
import org.estaos.pin.core.parser.ast.BreakStatement;
import org.estaos.pin.core.parser.ast.CallableCode;
import org.estaos.pin.core.parser.ast.CallableCodeExpression;
import org.estaos.pin.core.parser.ast.CallableType;
import org.estaos.pin.core.parser.ast.CharLiteralExpression;
import org.estaos.pin.core.parser.ast.CharSequenceLiteralExpression;
import org.estaos.pin.core.parser.ast.CompareEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareGreaterThanEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareGreaterThanExpression;
import org.estaos.pin.core.parser.ast.CompareLessThanEqualToExpression;
import org.estaos.pin.core.parser.ast.CompareLessThanExpression;
import org.estaos.pin.core.parser.ast.CompareNotEqualToExpression;
import org.estaos.pin.core.parser.ast.CompilationUnit;
import org.estaos.pin.core.parser.ast.ContinueStatement;
import org.estaos.pin.core.parser.ast.DivisionExpression;
import org.estaos.pin.core.parser.ast.DoWhileLoop;
import org.estaos.pin.core.parser.ast.ExplicitCastExpression;
import org.estaos.pin.core.parser.ast.Expression;
import org.estaos.pin.core.parser.ast.ForLoop;
import org.estaos.pin.core.parser.ast.FunctionCallExpression;
import org.estaos.pin.core.parser.ast.IfStatement;
import org.estaos.pin.core.parser.ast.Import;
import org.estaos.pin.core.parser.ast.LogicalAndExpression;
import org.estaos.pin.core.parser.ast.LogicalNotExpression;
import org.estaos.pin.core.parser.ast.LogicalOrExpression;
import org.estaos.pin.core.parser.ast.ModulusExpression;
import org.estaos.pin.core.parser.ast.MultiplyExpression;
import org.estaos.pin.core.parser.ast.NamedValueSymbol;
import org.estaos.pin.core.parser.ast.NullExpression;
import org.estaos.pin.core.parser.ast.NumberLiteralExpression;
import org.estaos.pin.core.parser.ast.ReturnStatement;
import org.estaos.pin.core.parser.ast.Source;
import org.estaos.pin.core.parser.ast.SubtractExpression;
import org.estaos.pin.core.parser.ast.SymbolValueExpression;
import org.estaos.pin.core.parser.ast.Type;
import org.estaos.pin.core.parser.ast.TypePassExpression;
import org.estaos.pin.core.parser.ast.TypeReference;
import org.estaos.pin.core.parser.ast.VariableDeclaration;
import org.estaos.pin.core.parser.ast.WhileLoop;
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

public class ASTBuilderVisitor implements PinLangParserVisitor<Object> {
    /// The file being parsed
    private final File file;
    private final List<FunctionDefinition> pendingFunctionDefinitions = new LinkedList<>();

    public static final String CALLABLE_TYPE_SIGIL = "_type__";
    public static final String CALLABLE_CODE_SIGIL = "_code__";

    public ASTBuilderVisitor(File file) {
        this.file = file;
    }

    @Override
    public CompilationUnit visitCompilationUnit(PinLangParser.CompilationUnitContext ctx) {
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
    public Import visitLanguageImport(PinLangParser.LanguageImportContext ctx) {
        File importFile = getImportFile(ctx.importPath());
        Source source = getNodeSource(file, ctx);
        return new Import("", false, source, importFile, null);
    }

    @Override
    public Import visitExternalImport(PinLangParser.ExternalImportContext ctx) {
        File importFile = getImportFile(ctx.importPath());
        Source source = getNodeSource(file, ctx);
        return new Import("", true, source, importFile, null);
    }

    @Override
    public NamedValueSymbol visitVariableDeclaration(PinLangParser.VariableDeclarationContext ctx) {
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
    public NamedValueSymbol visitVariableDeclarationWithNoInitialisation(PinLangParser.VariableDeclarationWithNoInitialisationContext ctx) {
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
    public NamedValueSymbol visitVariableDeclarationWithInitialisation(PinLangParser.VariableDeclarationWithInitialisationContext ctx) {
        String variableName = visitVariableName(ctx.variableName());
        @Nullable TypeReference typeReference = Optional.ofNullable(ctx.typeReference())
                .map(this::visitTypeReference)
                .orElse(null);

        Expression value = visitExpression(ctx.expression());

        return new NamedValueSymbol(
                variableName, typeReference,
                getNodeSource(file, ctx), true, false,
                "", value, false);
    }

    @Override
    public Expression visitExpression(PinLangParser.ExpressionContext ctx) {
        if(ctx.primaryExpression() != null) {
            return visitPrimaryExpression(ctx.primaryExpression());
        } else if(ctx.typePassExpression() != null) {
            return visitTypePassExpression(ctx.typePassExpression());
        } else if(ctx.statementsBlock() != null && ctx.anonymousFunctionHeader() != null) {
            // Anonymous function
            FunctionDefinition functionDefinition = getAnonymousFunction(ctx.anonymousFunctionHeader(), ctx.statementsBlock());
            pendingFunctionDefinitions.add(functionDefinition);
            return new SymbolValueExpression(getNodeSource(file, ctx), Objects.requireNonNull(functionDefinition.symbol()).getName());
        } else if(!ctx.expression().isEmpty()) {
            Expression left = visitExpression(ctx.expression().getFirst());
            if(ctx.expression().size() > 1) {
                Expression right = visitExpression(ctx.expression().get(1));
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
    public Expression visitPrimaryExpression(PinLangParser.PrimaryExpressionContext ctx) {
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
    public Expression visitStatement(PinLangParser.StatementContext ctx) {
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
        } else if(ctx.whileLoopStatement() != null) {
            return visitWhileLoopStatement(ctx.whileLoopStatement());
        } else if(ctx.doWhileLoopStatement() != null) {
            return visitDoWhileLoopStatement(ctx.doWhileLoopStatement());
        } else if(ctx.forLoopStatement() != null) {
            return visitForLoopStatement(ctx.forLoopStatement());
        } else {
            throw new IllegalArgumentException("Unknown statement " + ctx);
        }
    }

    @Override
    public IfStatement visitIfStatement(PinLangParser.IfStatementContext ctx) {
        Expression expression = visitExpression(ctx.expression());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());

        @Nullable BlockExpression elseBlock = Optional.ofNullable(ctx.elseBlock())
                .map(this::visitElseBlock)
                .orElse(null);

        List<IfStatement.ElseIfBlock> elseIfBlocks = ctx.elseIfBlock().stream().map(this::visitElseIfBlock).toList();

        return new IfStatement(getNodeSource(file, ctx), expression, blockExpression, elseBlock, elseIfBlocks);
    }

    @Override
    public WhileLoop visitWhileLoopStatement(PinLangParser.WhileLoopStatementContext ctx) {
        Expression expression = visitExpression(ctx.expression());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());
        return new WhileLoop(getNodeSource(file, ctx), expression, blockExpression);
    }

    @Override
    public DoWhileLoop visitDoWhileLoopStatement(PinLangParser.DoWhileLoopStatementContext ctx) {
        Expression expression = visitExpression(ctx.expression());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());
        return new DoWhileLoop(getNodeSource(file, ctx), expression, blockExpression);
    }

    @Override
    public ForLoop visitForLoopStatement(PinLangParser.ForLoopStatementContext ctx) {
        Source source = getNodeSource(file, ctx);

        @Nullable VariableDeclaration declarationExpression = null;
        if(ctx.variableDeclaration() != null) {
            declarationExpression =
                    new VariableDeclaration(source, visitVariableDeclaration(ctx.variableDeclaration()));
        }

        // TODO: For now parser does not allow single expression because we need to get this
        // code to be able to tell which expression (first or last) was passed.
        @Nullable Expression comparisonExpression = null;
        if(!ctx.expression().isEmpty()) {
            comparisonExpression = visitExpression(ctx.expression().getFirst());
        }

        @Nullable Expression counterExpression = null;
        if(ctx.expression().size() > 1) {
            counterExpression = visitExpression(ctx.expression().get(1));
        }

        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());

        return new ForLoop(source, declarationExpression, comparisonExpression, counterExpression, blockExpression);
    }

    @Override
    public ReturnStatement visitReturnStatement(PinLangParser.ReturnStatementContext ctx) {
        return new ReturnStatement(getNodeSource(file, ctx), visitExpression(ctx.expression()));
    }

    @Override
    public VariableDeclaration visitVariableDeclarationStatement(PinLangParser.VariableDeclarationStatementContext ctx) {
        return new VariableDeclaration(
                getNodeSource(file, ctx),
                visitVariableDeclaration(ctx.variableDeclaration()));
    }

    @Override
    public ContinueStatement visitContinueStatement(PinLangParser.ContinueStatementContext ctx) {
        return new ContinueStatement(getNodeSource(file, ctx));
    }

    @Override
    public BreakStatement visitBreakStatement(PinLangParser.BreakStatementContext ctx) {
        return new BreakStatement(getNodeSource(file, ctx));
    }

    @Override
    public Expression visitExpressionStatement(PinLangParser.ExpressionStatementContext ctx) {
        return visitExpression(ctx.expression());
    }

    @Override
    public NumberLiteralExpression visitNumberLiteralExpression(PinLangParser.NumberLiteralExpressionContext ctx) {
        return new NumberLiteralExpression(getNodeSource(file, ctx), ctx.NUMBER().getText());
    }

    @Override
    public SymbolValueExpression visitSymbolValueExpression(PinLangParser.SymbolValueExpressionContext ctx) {
        return new SymbolValueExpression(getNodeSource(file, ctx), ctx.IDENTIFIER().getText());
    }

    @Override
    public CharSequenceLiteralExpression visitCharSequenceExpression(PinLangParser.CharSequenceExpressionContext ctx) {
        if(ctx.singleLineCharSequenceExpression() != null) {
            return visitSingleLineCharSequenceExpression(ctx.singleLineCharSequenceExpression());
        } else {
            return visitMultilineCharSequenceExpression(ctx.multilineCharSequenceExpression());
        }
    }

    @Override
    public CharSequenceLiteralExpression visitSingleLineCharSequenceExpression(PinLangParser.SingleLineCharSequenceExpressionContext ctx) {
        return new CharSequenceLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.SINGLE_LINE_STRING().getText()));
    }

    @Override
    public CharSequenceLiteralExpression visitMultilineCharSequenceExpression(PinLangParser.MultilineCharSequenceExpressionContext ctx) {
        return new CharSequenceLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.MULTI_LINE_STRING().getText()));
    }

    @Override
    public TypePassExpression visitTypePassExpression(PinLangParser.TypePassExpressionContext ctx) {
        return new TypePassExpression(
                getNodeSource(file, ctx), new TypeReference(
                ctx.IDENTIFIER().getText(),
                null,0, List.of()
        ));
    }

    @Override
    public NullExpression visitNullExpression(PinLangParser.NullExpressionContext ctx) {
        return new NullExpression(getNodeSource(file, ctx));
    }

    @Override
    public BooleanLiteralExpression visitBooleanLiteralExpression(PinLangParser.BooleanLiteralExpressionContext ctx) {
        return new BooleanLiteralExpression(getNodeSource(file, ctx), ctx.FALSE_() == null);
    }

    @Override
    public CharLiteralExpression visitCharLiteralExpression(PinLangParser.CharLiteralExpressionContext ctx) {
        return new CharLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.CHAR().getText()).charAt(0));
    }

    @Override
    public TypeReference visitExplicitTypeCastSigil(PinLangParser.ExplicitTypeCastSigilContext ctx) {
        return visitTypeReference(ctx.typeReference());
    }

    @Override
    public String visitVariableName(PinLangParser.VariableNameContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public TypeReference visitTypeReference(PinLangParser.TypeReferenceContext ctx) {
        if(ctx.nonArrayTypeReference() != null) {
            return visitNonArrayTypeReference(ctx.nonArrayTypeReference());
        } else {
            return visitArrayTypeReference(ctx.arrayTypeReference());
        }
    }

    @Override
    public TypeReference visitNonArrayTypeReference(PinLangParser.NonArrayTypeReferenceContext ctx) {
        return new TypeReference(ctx.IDENTIFIER().getText(), null, 0, List.of());
    }

    @Override
    public TypeReference visitArrayTypeReference(PinLangParser.ArrayTypeReferenceContext ctx) {
        int dimensionsCount = ctx.arrayIndexingWithOptionalIndex().size();
        return new TypeReference(ctx.IDENTIFIER().getText(), null, dimensionsCount, List.of());
    }

    @Override
    public CallableType visitFunctionTypeDef(PinLangParser.FunctionTypeDefContext ctx) {
        String typeName = visitVariableName(ctx.functionHeader().variableName());
        CallableType callableType = visitFunctionHeader(ctx.functionHeader());
        callableType.setName(typeName);
        return callableType;
    }

    @Override
    public FunctionDefinition visitFunctionDefinition(PinLangParser.FunctionDefinitionContext ctx) {
        String symbolName = visitVariableName(ctx.functionHeader().variableName());
        CallableType callableType = visitFunctionHeader(ctx.functionHeader());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());
        Source source = getNodeSource(file, ctx);

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    @Override
    public CallableType visitFunctionHeader(PinLangParser.FunctionHeaderContext ctx) {
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
    public CallableType visitAnonymousFunctionHeader(PinLangParser.AnonymousFunctionHeaderContext ctx) {
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
    public List<NamedValueSymbol> visitFunctionParameterList(PinLangParser.FunctionParameterListContext ctx) {
        return ctx.functionParameter().stream().map(this::visitFunctionParameter).toList();
    }

    @Override
    public NamedValueSymbol visitFunctionParameter(PinLangParser.FunctionParameterContext ctx) {
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
    public List<Argument> visitFunctionCallArgumentEnclosure(PinLangParser.FunctionCallArgumentEnclosureContext ctx) {
        return ctx.expression().stream().map(this::visitExpression).map(Argument::new).toList();
    }

    @Override
    public TypeReference visitFunctionReturnType(PinLangParser.FunctionReturnTypeContext ctx) {
        return visitTypeReference(ctx.typeReference());
    }

    @Override
    public BlockExpression visitStatementsBlock(PinLangParser.StatementsBlockContext ctx) {
        return new BlockExpression(getNodeSource(file, ctx), ctx.statement().stream().map(this::visitStatement).toList());
    }

    @Override
    public IfStatement.ElseIfBlock visitElseIfBlock(PinLangParser.ElseIfBlockContext ctx) {
        Expression expression = visitExpression(ctx.expression());
        BlockExpression blockExpression = visitStatementsBlock(ctx.statementsBlock());

        return new IfStatement.ElseIfBlock(expression, blockExpression);
    }

    @Override
    public BlockExpression visitElseBlock(PinLangParser.ElseBlockContext ctx) {
        return visitStatementsBlock(ctx.statementsBlock());
    }

    @Override
    public Object visitArrayIndexingWithOptionalIndex(PinLangParser.ArrayIndexingWithOptionalIndexContext ctx) {
        return null;
    }

    @Override
    public String visitImportPath(PinLangParser.ImportPathContext ctx) {
        return unquoteString(ctx.getText());
    }

    @Override
    public String visitDocumentationCommentLines(PinLangParser.DocumentationCommentLinesContext ctx) {
        return ctx.MULTI_LINE_COMMENT_LINE().stream().map(ParseTree::getText).collect(Collectors.joining("\n"));
    }

    @Override
    public Object visitFunctionVarArgsIndicator(PinLangParser.FunctionVarArgsIndicatorContext ctx) {
        return null;
    }

    @Override
    public CompilationUnit visit(ParseTree parseTree) {
        return visitCompilationUnit((PinLangParser.CompilationUnitContext) parseTree);
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

    private File getImportFile(PinLangParser.ImportPathContext importPathCtx) {
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

    private Expression getBinaryExpression(PinLangParser.ExpressionContext ctx, Expression left, Expression right) {
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

    private Expression getUnaryExpression(Expression operand, PinLangParser.ExpressionContext ctx) {
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

    private ExplicitCastExpression getExplicitCast(Expression operand, PinLangParser.ExplicitTypeCastSigilContext ctx) {
        return new ExplicitCastExpression(getNodeSource(file, ctx), operand, visitExplicitTypeCastSigil(ctx));
    }

    private FunctionDefinition getAnonymousFunction(PinLangParser.AnonymousFunctionHeaderContext headerContext, PinLangParser.StatementsBlockContext statementsBlockContext) {
        BlockExpression blockExpression = visitStatementsBlock(statementsBlockContext);
        CallableType callableType = visitAnonymousFunctionHeader(headerContext);
        String symbolName = getRandomSymbolName();
        Source source = getNodeSource(file, headerContext);

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    private FunctionDefinition getLambda(PinLangParser.AnonymousFunctionHeaderContext headerContext, Expression returnValue) {
        CallableType callableType = visitAnonymousFunctionHeader(headerContext);
        String symbolName = getRandomSymbolName();
        Source source = getNodeSource(file, headerContext);

        BlockExpression blockExpression = new BlockExpression(source, List.of(
                new ReturnStatement(source, returnValue)
        ));

        return getFunctionDefinition(source, symbolName, callableType, blockExpression);
    }

    private FunctionCallExpression getFunctionCall(ParserRuleContext ctx, Expression callableExpression, PinLangParser.FunctionCallArgumentEnclosureContext functionCallArgumentEnclosureContext) {
        List<Argument> arguments = visitFunctionCallArgumentEnclosure(functionCallArgumentEnclosureContext);
        return new FunctionCallExpression(getNodeSource(file, ctx), arguments, callableExpression);
    }

    public record FunctionDefinition(CallableType callableType, CallableCode callableCode, @Nullable NamedValueSymbol symbol) {}
}
