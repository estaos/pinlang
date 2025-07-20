package com.oreal.escript.parser;

import com.oreal.escript.antlr.*;
import com.oreal.escript.parser.ast.CompilationUnit;
import com.oreal.escript.parser.ast.Import;
import com.oreal.escript.parser.ast.Source;
import lombok.AllArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class ASTBuilderVisitor implements EScriptParserVisitor<Object> {
    /// The file being parsed
    private final File file;

    @Override
    public CompilationUnit visitCompilationUnit(EScriptParser.CompilationUnitContext ctx) {
        List<Import> imports = new LinkedList<>();
        imports.addAll(ctx.languageImport().stream().map(this::visitLanguageImport).toList());
        imports.addAll(ctx.externalImport().stream().map(this::visitExternalImport).toList());

        return new CompilationUnit(file, imports, List.of(), List.of());
    }

    @Override
    public Import visitLanguageImport(EScriptParser.LanguageImportContext ctx) {
        File importFile = getImportFile(ctx.SINGLE_LINE_STRING().getText(), ctx);
        Source source = getNodeSource(file, ctx);
        return new Import("", false, source, importFile, null);
    }

    @Override
    public Import visitExternalImport(EScriptParser.ExternalImportContext ctx) {
        File importFile = getImportFile(ctx.SINGLE_LINE_STRING().getText(), ctx);
        Source source = getNodeSource(file, ctx);
        return new Import("", true, source, importFile, null);
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

    private File getImportFile(String singleLineStringWithQuotes, ParserRuleContext ctx) {
        String parent = Optional.ofNullable(Path.of(file.getPath()).getParent())
                .map(Path::toString)
                .orElse("");

        Path importPath = Path.of(parent, unquoteSingleLineString(singleLineStringWithQuotes));
        return importPath.toFile();
//        return getNodeSource(importPath.toFile(), ctx);
    }
}
