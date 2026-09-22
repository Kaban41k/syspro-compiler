package kbn;

import java.util.List;

public class Jsonizer {
    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public String tokensToJson(List<Lexer.Token> tokens) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");

        for (int i = 0; i < tokens.size(); i++) {
            Lexer.Token t = tokens.get(i);

            sb.append("{");
            sb.append("\"kind\": \"").append(t.kind.name()).append("\", ");
            sb.append("\"value\": \"").append(escape(t.value)).append("\", ");
            sb.append("\"line\": ").append(t.line).append(", ");
            sb.append("\"column\": ").append(t.column);
            sb.append("}");

            if (i < tokens.size() - 1) sb.append(',');

            sb.append('\n');
        }
        sb.append("]\n");

        return sb.toString();
    }
}
