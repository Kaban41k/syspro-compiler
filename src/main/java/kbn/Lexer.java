package kbn;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    public enum TokenKind {
        // literals
        IDENT, INT,

        // keywords
        VAL, VAR, RETURN,

        // operators
        PLUS, MINUS, MULT, DIV, ASSIGN,

        // punctuation
        LPAREN, RPAREN,
        SEMI,

        // special
        EOF, ERROR
    }

    // --- Token ---

    public static final class Token {
        public final TokenKind kind;
        public final String value;
        public final int line;
        public final int column;

        public Token(TokenKind kind, String value, int line, int column) {
            this.kind = kind;
            this.value = value;
            this.line = line;
            this.column = column;
        }
    }

    // --- Char Functions ---

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == '_';
    }

    private static boolean isIdentStart(char c) {
        return isLetter(c);
    }

    private static boolean isIdentPart(char c) {
        return isLetter(c) || isDigit(c);
    }

    private static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' ||
                c == '\t' || c == '\r';
    }

    // --- Lexer state ---

    private final String src;
    private int pos;
    private int line;
    private int col;

    public Lexer(String src) {
        this.src = src;
        this.pos = 0;
        this.line = 1;
        this.col = 1;
    }

    private char peek() {
        if (pos >= src.length()) return 0;
        return src.charAt(pos);
    }

    private char peekNext() {
        pos++;
        char c = peek();
        pos--;
        return c;
    }

    private void next() {
        if (peek() == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        pos++;
    }

    private char peekAndGo() {
        char c = peek();
        next();
        return c;
    }

    private void skipSpaces() {
        while (isWhitespace(peek())) {
            next();
        }
    }

    private String skipComments() {
         if (peek() == '/') {
            char nextChar = peekNext();

            // single-line comment
            if (nextChar == '/') {
                next();
                next();
                while (peek() != '\n' && peek() != 0) {
                    next();
                }
                return null;
            }

            // multi-line comment
            if (nextChar == '*') {
                next();
                next();

                // skip comment body
                while ((peek() != '*' || peekNext() != '/') && peek() != 0) {
                    next();
                }

                // unterminated block comment error
                if (peek() == 0) {
                    return "Unterminated multi-line comment";
                }

                // skip "*/"
                next();
                next();
                next();
            }
         }

         return null;
    }

    private String readIdent() {
        StringBuilder str = new StringBuilder();
        char c;
        while (isIdentPart(c = peek())) {
            str.append(c);
            next();
        }
        return str.toString();
    }

    private String readInt() {
        StringBuilder str = new StringBuilder();
        char c;
        while (isDigit(c = peek())) {
            str.append(c);
            next();
        }
        return str.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            skipSpaces();
            int startLine = line, startCol = col;
            String err = skipComments();

            while (err != null) {
                tokens.add(new Token(TokenKind.ERROR, err, startLine, startCol));
                skipSpaces();
                startLine = line;
                startCol = col;
                err = skipComments();
            }

            startLine = line;
            startCol = col;

            char c = peek();

            // eof
            if (c == 0) {
                tokens.add(new Token(TokenKind.EOF, "", startLine, startCol));
                return tokens;
            }

            // ident || keyword
            if (isIdentStart(c)) {
                String ident = readIdent();

                if (ident.equals(TokenKind.RETURN.name().toLowerCase())) {
                    tokens.add(new Token(TokenKind.RETURN, ident, startLine, startCol));
                    continue;
                }

                if (ident.equals(TokenKind.VAL.name().toLowerCase())) {
                    tokens.add(new Token(TokenKind.VAL, ident, startLine, startCol));
                    continue;
                }

                if (ident.equals(TokenKind.VAR.name().toLowerCase())) {
                    tokens.add(new Token(TokenKind.VAR, ident, startLine, startCol));
                    continue;
                }

                tokens.add(new Token(TokenKind.IDENT, ident, startLine, startCol));
                continue;
            }

            // int
            if (isDigit(c)) {
                String integer = readInt();
                tokens.add(new Token(TokenKind.INT, integer, startLine, startCol));
                continue;
            }

            next();

            // operators
            if (c == '+') {
                tokens.add(new Token(TokenKind.PLUS, "+", startLine, startCol));
                continue;
            }

            if (c == '-') {
                tokens.add(new Token(TokenKind.MINUS, "-", startLine, startCol));
                continue;
            }

            if (c == '*') {
                tokens.add(new Token(TokenKind.MULT, "*", startLine, startCol));
                continue;
            }

            if (c == '/') {
                tokens.add(new Token(TokenKind.DIV, "/", startLine, startCol));
                continue;
            }

            if (c == '=') {
                tokens.add(new Token(TokenKind.ASSIGN, "=", startLine, startCol));
                continue;
            }

            // punctuation
            if (c == ';') {
                tokens.add(new Token(TokenKind.SEMI, ";", startLine, startCol));
                continue;
            }

            if (c == '(') {
                tokens.add(new Token(TokenKind.LPAREN, "(", startLine, startCol));
                continue;
            }

            if (c == ')') {
                tokens.add(new Token(TokenKind.RPAREN, ")", startLine, startCol));
                continue;
            }

            tokens.add(new Token(TokenKind.ERROR, String.valueOf(c), startLine, startCol));
        }
    }


}