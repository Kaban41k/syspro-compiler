package com.github.kaban41k.syspro.compiler;

import com.github.kaban41k.syspro.compiler.ast.AstErrors;
import com.github.kaban41k.syspro.compiler.ast.SemanticErrors;
import com.github.kaban41k.syspro.compiler.codegen.LlvmGenerator;
import com.github.kaban41k.syspro.compiler.lexer.Jsonizer;
import com.github.kaban41k.syspro.compiler.lexer.Lexer;

import com.github.kaban41k.syspro.compiler.token.Token;
import com.github.kaban41k.syspro.compiler.token.TokenKind;

import com.github.kaban41k.syspro.compiler.ast.AstJsonizer;
import com.github.kaban41k.syspro.compiler.ast.ProgramNode;

import com.github.kaban41k.syspro.compiler.parser.Parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException e) {
            System.err.println("I/O error: " + e);
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        Integer grammarVersion = null;
        String tokensOut = null;
        String astOut = null;
        String llvmOut = null;
        String srcIn = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-g" -> {
                        if (i + 1 >= args.length) wrongUsage("missing value for -g");
                        grammarVersion = Integer.parseInt(args[++i]);
                    }
                case "-t" -> {
                    if (i + 1 >= args.length) wrongUsage("missing value for -t");
                    tokensOut = args[++i];
                }
                case "-a" -> {
                    if (i + 1 >= args.length) wrongUsage("missing value for -a");
                    astOut = args[++i];
                }
                case "-o" -> {
                    if (i + 1 >= args.length) wrongUsage("missing value for -o");
                    llvmOut = args[++i];
                }
                default -> {
                    if (args[i].startsWith("-")) {
                        System.err.println("unknown flag: " + args[i]);
                        System.exit(2);
                    }
                    srcIn = args[i];
                }
            }
        }

        if (srcIn == null || (tokensOut == null && astOut == null && llvmOut == null)) {
            wrongUsage("missing input or output flag");
            return;
        }

        if (grammarVersion != null && grammarVersion != 1) {
            wrongUsage("grammar version " + grammarVersion + " not supported");
        }

        String code = Files.readString(Path.of(srcIn));
        List<Token> tokens = new Lexer(code).tokenize();

        if (tokensOut != null) {
            writeOut(tokensOut, Jsonizer.tokensToJson(tokens));
        }

        if (tokens.stream().anyMatch(t -> t.kind() == TokenKind.ERROR)) {
            System.exit(1);
        }

        Parser parser = null;
        ProgramNode ast = null;

        if (astOut != null || llvmOut != null) {
            parser = new Parser(tokens);
            ast = parser.parse();
        }

        if (astOut != null) {
            writeOut(astOut, AstJsonizer.toJson(ast));

            if (AstErrors.hasErrors(ast)
                    || SemanticErrors.hasErrors(ast)
                    || !parser.errors().isEmpty()) {
                System.exit(1);
            }
        }

        if (llvmOut != null) {
            String ir = new LlvmGenerator().generate(ast);
            writeOut(llvmOut, ir);
        }
    }

    private static void writeOut(String path, String content) throws IOException {
        Path out = Path.of(path);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.writeString(out, content);
    }

    private static void wrongUsage(String msg) {
        System.err.println("error: " + msg);
        System.err.println("usage: splc -g <ver> [-t <tokens_out>] [-a <ast_out>] <input.spl>");
        System.exit(2);
    }
}