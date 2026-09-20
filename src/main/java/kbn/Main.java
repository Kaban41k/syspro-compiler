package kbn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
    private static String tokensToJson(List<Lexer.Token> tokens) {
        StringBuilder str = new StringBuilder();
        str.append("[\n");
        for (int i = 0; i < tokens.size(); i++) {
            Lexer.Token t = tokens.get(i);
            str.append("{");
            str.append("\"kind\": \"").append(t.kind.name()).append("\", ");
            str.append("\"value\": \"").append(t.value).append("\", ");
            str.append("\"line\": ").append(t.line).append(", ");
            str.append("\"column\": ").append(t.column);
            str.append("}");
            if (i < tokens.size() - 1) str.append(',');
            str.append('\n');
        }
        str.append("]\n");
        return str.toString();
    }

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
        List<Lexer.Token> tokens = lexer.tokenize();

        String json = tokensToJson(tokens);
        Files.writeString(tokensOut, json);

        if (hasErrorToken(tokens)) {
            System.exit(1);
        }
    }
}