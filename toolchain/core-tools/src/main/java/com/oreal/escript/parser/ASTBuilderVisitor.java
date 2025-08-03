package com.oreal.escript.parser;

import com.oreal.escript.antlr.*;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Expression;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.NamedValueSymbol;
import com.oreal.escript.parser.ast.NumberLiteralExpression;
import com.oreal.escript.parser.ast.Source;
import com.oreal.escript.parser.ast.Symbol;
import com.oreal.escript.parser.ast.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
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

    @Override
    public CompilationUnit visitCompilationUnit(EScriptParser.CompilationUnitContext ctx) {
        List<Import> imports = new LinkedList<>();
        imports.addAll(ctx.languageImport().stream().map(this::visitLanguageImport).toList());
        imports.addAll(ctx.externalImport().stream().map(this::visitExternalImport).toList());

        List<? extends Symbol> symbols = ctx.variableDeclaration().stream().map(this:: visitVariableDeclaration).toList();

        return new CompilationUnit(file, imports, symbols, List.of());
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
        ParserTypeReference typeReference = Optional.ofNullable(ctx.nonArrayTypeReference())
                .map(this::visitNonArrayTypeReference)
                .orElseGet(() -> visitArrayTypeReference(ctx.arrayTypeReference()));

        return new NamedValueSymbol(
                variableName, typeReference.typeReference,
                getNodeSource(file, ctx), true, false,
                "", null, false,
                typeReference.arrayDimensions);
    }

    @Override
    public NamedValueSymbol visitVariableDeclarationWithInitialisation(EScriptParser.VariableDeclarationWithInitialisationContext ctx) {
        String variableName = visitVariableName(ctx.variableName());
        ParserTypeReference typeReference;
        if(ctx.nonArrayTypeReference() != null) {
            typeReference = visitNonArrayTypeReference(ctx.nonArrayTypeReference());
        } else if(ctx.arrayTypeReference() != null) {
            typeReference = visitArrayTypeReference(ctx.arrayTypeReference());
        } else {
            // Type was not given, need to be inferred.
            typeReference = new ParserTypeReference(null, 0);
        }

        Expression value = visitExpression(ctx.expression());

        return new NamedValueSymbol(
                variableName, typeReference.typeReference,
                getNodeSource(file, ctx), true, false,
                "", value, false,
                typeReference.arrayDimensions);
    }

    @Override
    public Expression visitExpression(EScriptParser.ExpressionContext ctx) {
        if(ctx.numberLiteralExpression() != null) {
            return visitNumberLiteralExpression(ctx.numberLiteralExpression());
        } else {
            throw new IllegalArgumentException("Unknown expression " + ctx);
        }
    }

    @Override
    public NumberLiteralExpression visitNumberLiteralExpression(EScriptParser.NumberLiteralExpressionContext ctx) {
        return new NumberLiteralExpression(ctx.NUMBER().getText());
    }

    @Override
    public String visitVariableName(EScriptParser.VariableNameContext ctx) {
        return ctx.IDENTIFIER().getText();
    }

    @Override
    public ParserTypeReference visitNonArrayTypeReference(EScriptParser.NonArrayTypeReferenceContext ctx) {
        TypeReference typeReference = new TypeReference(ctx.IDENTIFIER().getText(), null, List.of());
        return new ParserTypeReference(typeReference, 0);
    }

    @Override
    public ParserTypeReference visitArrayTypeReference(EScriptParser.ArrayTypeReferenceContext ctx) {
        int dimensionsCount = ctx.arrayIndexingWithOptionalIndex().size();
        TypeReference typeReference = new TypeReference(ctx.IDENTIFIER().getText(), null, List.of());

        return new ParserTypeReference(typeReference, dimensionsCount);
    }

    @Override
    public Object visitArrayIndexingWithOptionalIndex(EScriptParser.ArrayIndexingWithOptionalIndexContext ctx) {
        return null;
    }

    @Override
    public String visitImportPath(EScriptParser.ImportPathContext ctx) {
        return unquoteSingleLineString(ctx.getText());
    }

    @Override
    public String visitDocumentationCommentLines(EScriptParser.DocumentationCommentLinesContext ctx) {
        return ctx.MULTI_LINE_COMMENT_LINE().stream().map(ParseTree::getText).collect(Collectors.joining("\n"));
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

    private String unquoteSingleLineString(String singleLineStringWithQuotes) {
        return singleLineStringWithQuotes.substring(1, singleLineStringWithQuotes.length()-1);
    }

    private File getImportFile(EScriptParser.ImportPathContext importPathCtx) {
        String parent = Optional.ofNullable(Path.of(file.getPath()).getParent())
                .map(Path::toString)
                .orElse("");

        Path importPath = Path.of(parent, visitImportPath(importPathCtx));
        return importPath.toFile();
    }

    @AllArgsConstructor
    @Data
    public static class ParserTypeReference {
        private @Nullable TypeReference typeReference;
        private int arrayDimensions;
    }
}
