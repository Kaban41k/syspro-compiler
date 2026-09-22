package kbn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static boolean hasErrorToken(List<Lexer.Token> tokens) {
        for (Lexer.Token t : tokens) {
            if (t.kind == Lexer.TokenKind.ERROR) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) throws IOException {
        String code = Files.readString(Path.of(args[args.length - 1]));
        Path tokensOut = Path.of(args[args.length - 2]);

        Lexer lexer = new Lexer(code);
        Jsonizer jsonizer = new Jsonizer();

        List<Lexer.Token> tokens = lexer.tokenize();

        Files.writeString(tokensOut, jsonizer.tokensToJson(tokens));

        if (hasErrorToken(tokens)) {
            System.exit(1);
        }
    }
}