package com.oreal.escript.parser;

import com.oreal.escript.antlr.*;
import com.oreal.escript.parser.ast.Argument;
import com.oreal.escript.parser.ast.AssignmentExpression;
import com.oreal.escript.parser.ast.BlockExpression;
import com.oreal.escript.parser.ast.BooleanLiteralExpression;
import com.oreal.escript.parser.ast.CallableCode;
import com.oreal.escript.parser.ast.CallableCodeExpression;
import com.oreal.escript.parser.ast.CallableType;
import com.oreal.escript.parser.ast.CharLiteralExpression;
import com.oreal.escript.parser.ast.CharSequenceLiteralExpression;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.ExplicitCastExpression;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.FunctionCallExpression;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.ReturnStatement;
import com.oreal.escript.parser.ast.Source;
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
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ASTBuilderVisitor implements EScriptParserVisitor<Object> {
    /// The file being parsed
    private final File file;

    public static final String CALLABLE_TYPE_SIGIL = "_type__";
    public static final String CALLABLE_CODE_SIGIL = "_code__";

    @Override
    public CompilationUnit visitCompilationUnit(EScriptParser.CompilationUnitContext ctx) {
        List<Import> imports = new LinkedList<>();
        imports.addAll(ctx.languageImport().stream().map(this::visitLanguageImport).toList());
        imports.addAll(ctx.externalImport().stream().map(this::visitExternalImport).toList());

        List<NamedValueSymbol> symbols = new LinkedList<>(ctx.variableDeclaration().stream().map(this::visitVariableDeclaration).toList());
        List<Type> types = new LinkedList<>(ctx.functionTypeDef().stream().map(this::visitFunctionTypeDef).toList());
        List<CallableCode> callableCodeBlocks = new LinkedList<>();

        List<FunctionDefinition> functionDefinitions = ctx.functionDefinition().stream().map(this::visitFunctionDefinition).toList();
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

        Expression value = visitExpression(ctx.expression());

        return new NamedValueSymbol(
                variableName, typeReference,
                getNodeSource(file, ctx), true, false,
                "", value, false);
    }

    @Override
    public Expression visitExpression(EScriptParser.ExpressionContext ctx) {
        Expression expression;
        if(ctx.primaryExpression() != null) {
            expression = visitPrimaryExpression(ctx.primaryExpression());
        } else {
            expression = visitFunctionCallExpression(ctx.functionCallExpression());
        }

        if(ctx.explicitTypeCastSigil() != null) {
            return new ExplicitCastExpression(
                    getNodeSource(file, ctx),
                    expression,
                    visitExplicitTypeCastSigil(ctx.explicitTypeCastSigil()));
        } else {
            return expression;
        }
    }

    @Override
    public Expression visitPrimaryExpression(EScriptParser.PrimaryExpressionContext ctx) {
        if(ctx.numberLiteralExpression() != null) {
            return visitNumberLiteralExpression(ctx.numberLiteralExpression());
        } else if(ctx.charSequenceExpression() != null) {
            return visitCharSequenceExpression(ctx.charSequenceExpression());
        } else if(ctx.symbolValueExpression() != null) {
            return visitSymbolValueExpression(ctx.symbolValueExpression());
        } else if(ctx.typePassExpression() != null) {
            return visitTypePassExpression(ctx.typePassExpression());
        } else if(ctx.booleanLiteralExpression() != null) {
            return visitBooleanLiteralExpression(ctx.booleanLiteralExpression());
        } else if(ctx.charLiteralExpression() != null) {
            return visitCharLiteralExpression(ctx.charLiteralExpression());
        } else if(ctx.assignmentExpression() != null) {
            return visitAssignmentExpression(ctx.assignmentExpression());
        } else {
            throw new IllegalArgumentException("Unknown expression " + ctx);
        }
    }

    @Override
    public Expression visitStatement(EScriptParser.StatementContext ctx) {
        if(ctx.returnStatement() != null) {
            return visitReturnStatement(ctx.returnStatement());
        } else if(ctx.functionCallStatement() != null) {
            return visitFunctionCallStatement(ctx.functionCallStatement());
        } else if(ctx.variableDeclarationStatement() != null) {
            return visitVariableDeclarationStatement(ctx.variableDeclarationStatement());
        } else if(ctx.statementsBlock() != null) {
            return visitStatementsBlock(ctx.statementsBlock());
        } else if(ctx.assignmentStatement() != null) {
            return visitAssignmentStatement(ctx.assignmentStatement());
        } else {
            throw new IllegalArgumentException("Unknown statement " + ctx);
        }
    }

    @Override
    public FunctionCallExpression visitFunctionCallStatement(EScriptParser.FunctionCallStatementContext ctx) {
        return visitFunctionCallExpression(ctx.functionCallExpression());
    }

    @Override
    public ReturnStatement visitReturnStatement(EScriptParser.ReturnStatementContext ctx) {
        return new ReturnStatement(getNodeSource(file, ctx), visitExpression(ctx.expression()));
    }

    @Override
    public VariableDeclaration visitVariableDeclarationStatement(EScriptParser.VariableDeclarationStatementContext ctx) {
        return new VariableDeclaration(
                getNodeSource(file, ctx),
                visitVariableDeclaration(ctx.variableDeclaration()));
    }

    @Override
    public AssignmentExpression visitAssignmentStatement(EScriptParser.AssignmentStatementContext ctx) {
        return visitAssignmentExpression(ctx.assignmentExpression());
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
    public BooleanLiteralExpression visitBooleanLiteralExpression(EScriptParser.BooleanLiteralExpressionContext ctx) {
        return new BooleanLiteralExpression(getNodeSource(file, ctx), ctx.FALSE_() == null);
    }

    @Override
    public CharLiteralExpression visitCharLiteralExpression(EScriptParser.CharLiteralExpressionContext ctx) {
        return new CharLiteralExpression(getNodeSource(file, ctx), unquoteString(ctx.CHAR().getText()).charAt(0));
    }

    @Override
    public AssignmentExpression visitAssignmentExpression(EScriptParser.AssignmentExpressionContext ctx) {
        Expression value;
        if(ctx.primaryExpression() != null) {
            value = visitPrimaryExpression(ctx.primaryExpression());
        } else {
            value = visitFunctionCallExpression(ctx.functionCallExpression());
        }

        return new AssignmentExpression(getNodeSource(file, ctx),
                visitVariableName(ctx.variableName()), value);
    }

    @Override
    public FunctionCallExpression visitFunctionCallExpression(EScriptParser.FunctionCallExpressionContext ctx) {
        Expression callableExpression;
        if(ctx.primaryExpression() != null) {
            callableExpression = visitPrimaryExpression(ctx.primaryExpression());
        } else {
            callableExpression = visitFunctionCallExpression(ctx.functionCallExpression());
        }

        if(ctx.functionCallArgumentList() == null) {
            return new FunctionCallExpression(getNodeSource(file, ctx), List.of(), callableExpression);
        } else {
            List<Argument> arguments = visitFunctionCallArgumentList(ctx.functionCallArgumentList());
            return new FunctionCallExpression(getNodeSource(file, ctx), arguments, callableExpression);
        }
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
        String typeName = String.format("%s_%s", symbolName, CALLABLE_TYPE_SIGIL);
        CallableType callableType = visitFunctionHeader(ctx.functionHeader());
        callableType.setName(typeName);

        boolean isMain = symbolName.equals("main");
        String callableBlockName = isMain ? symbolName : String.format("%s_%s", symbolName, CALLABLE_CODE_SIGIL);
        CallableCode callableCode = new CallableCode(
                callableBlockName,
                callableType.getSource(), callableType, visitStatementsBlock(ctx.statementsBlock()));

        NamedValueSymbol symbol = new NamedValueSymbol(
                symbolName,
                TypeReference.ofType(callableType),
                callableCode.getSource(),
                true, false, "",
                new CallableCodeExpression(getNodeSource(file, ctx), callableCode), false
        );

        if(isMain) {
            return new FunctionDefinition(callableType, callableCode, null);
        } else {
            return new FunctionDefinition(callableType, callableCode, symbol);
        }
    }

    @Override
    public CallableType visitFunctionHeader(EScriptParser.FunctionHeaderContext ctx) {
        Source source = getNodeSource(file, ctx);
        boolean isVarArgs = false;
        List<NamedValueSymbol> parameters = List.of();
        if(ctx.functionParameterList() != null) {
            parameters = visitFunctionParameterList(ctx.functionParameterList());

            if(ctx.functionParameterList().functionVarArgsIndicator() != null) {
                isVarArgs = true;
            }
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
    public List<Argument> visitFunctionCallArgumentList(EScriptParser.FunctionCallArgumentListContext ctx) {
        return ctx.expression().stream().map(this::visitExpression).map(Argument::new).toList();
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

    public record FunctionDefinition(CallableType callableType, CallableCode callableCode, @Nullable NamedValueSymbol symbol) {}
}
