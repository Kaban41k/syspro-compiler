package kbn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            run(args);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws IOException {
        Integer grammarVersion = null;
        String tokensOut = null;
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
                case "-a", "-o" -> {
                    System.err.println("flag " + args[i] + " not implemented yet");
                    System.exit(2);
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

        if (srcIn == null || tokensOut == null) {
            wrongUsage("missing input or -t");
            return;
        }

        if (grammarVersion != null && grammarVersion != 1) {
            wrongUsage("grammar version " + grammarVersion + " not supported");
        }

        String code = Files.readString(Path.of(srcIn));
        List<Lexer.Token> tokens = new Lexer(code).tokenize();

        Files.writeString(Path.of(tokensOut), Jsonizer.tokensToJson(tokens));

        if (tokens.stream().anyMatch(
                t -> t.kind == Lexer.TokenKind.ERROR)) {
            System.exit(1);
        }
    }

    private static void wrongUsage(String msg) {
        System.err.println("error: " + msg);
        System.err.println("usage: splc -g <ver> -t <tokens_out> <input.spl>");
        System.exit(2);
    }
}