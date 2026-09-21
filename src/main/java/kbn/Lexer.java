package kbn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final Map<String, TokenKind> KEYWORDS = Map.of(
            "val",    TokenKind.VAL,
            "var",    TokenKind.VAR,
            "return", TokenKind.RETURN
    );

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
        // ! DO NOT CHANGE POSITION !
        pos++;
        char c = peek();
        pos--;
        return c;
    }

    private void step() {
        if (peek() == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        pos++;
    }

    private void next(int n) {
        for (int i = 0; i < n; i++) step();
    }

    private void skipWhitespace() {
        while (isWhitespace(peek())) {
            step();
        }
    }

    private String skipComment() {
        if (peek() == '/') {
            char nextChar = peekNext();

            // single-line comment "//"
            if (nextChar == '/') {
                next("//".length());

                // skip comment body
                while (peek() != '\n' && peek() != 0) {
                    step();
                }

                return null;
            }

            // multi-line comment "/*"
            if (nextChar == '*') {
                next("/*".length());

                // skip comment body
                while ((peek() != '*' || peekNext() != '/') && peek() != 0) {
                    step();
                }

                // unterminated block comment error
                if (peek() == 0) {
                    return "Unterminated multi-line comment";
                }

                next("*/".length());
            }
        }

        return null;
    }

    private String readIdent() {
        assert isLetter(peek());

        StringBuilder str = new StringBuilder();
        char c;
        while (isIdentPart(c = peek())) {
            str.append(c);
            step();
        }
        return str.toString();
    }

    private String readInt() {
        assert isDigit(peek());

        StringBuilder str = new StringBuilder();
        char c;
        while (isDigit(c = peek())) {
            str.append(c);
            step();
        }
        return str.toString();
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            // skip whitespace and comments
            while (true) {
                int startLine = line, startCol = col;
                int before = pos;

                String err = skipComment();
                skipWhitespace();

                if (err == null) {
                    // no progress check
                    if (pos == before) break;
                    else continue;
                }

                tokens.add(new Token(TokenKind.ERROR, err, startLine, startCol));
                break;
            }

            int startLine = line, startCol = col;
            char c = peek();

            // eof
            if (c == 0) {
                tokens.add(new Token(TokenKind.EOF, "", startLine, startCol));
                return tokens;
            }

            // ident || keyword
            if (isLetter(c)) {
                String ident = readIdent();
                TokenKind kind = KEYWORDS.getOrDefault(ident, TokenKind.IDENT);
                tokens.add(new Token(kind, ident, startLine, startCol));
                continue;
            }

            // int
            if (isDigit(c)) {
                String integer = readInt();
                tokens.add(new Token(TokenKind.INT, integer, startLine, startCol));
                continue;
            }

            // single symbol tokens
            switch (c) {
                case '+' -> tokens.add(new Token(TokenKind.PLUS,   "+", startLine, startCol));
                case '-' -> tokens.add(new Token(TokenKind.MINUS,  "-", startLine, startCol));
                case '*' -> tokens.add(new Token(TokenKind.MULT,   "*", startLine, startCol));
                case '/' -> tokens.add(new Token(TokenKind.DIV,    "/", startLine, startCol));
                case '=' -> tokens.add(new Token(TokenKind.ASSIGN, "=", startLine, startCol));
                case ';' -> tokens.add(new Token(TokenKind.SEMI,   ";", startLine, startCol));
                case '(' -> tokens.add(new Token(TokenKind.LPAREN, "(", startLine, startCol));
                case ')' -> tokens.add(new Token(TokenKind.RPAREN, ")", startLine, startCol));
                default  -> tokens.add(new Token(TokenKind.ERROR, String.valueOf(c), startLine, startCol));
            }
            step();

            // continue
        }
    }
}