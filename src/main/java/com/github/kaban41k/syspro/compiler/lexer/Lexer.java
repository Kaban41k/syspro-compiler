package com.github.kaban41k.syspro.compiler.lexer;

import com.github.kaban41k.syspro.compiler.token.Token;
import com.github.kaban41k.syspro.compiler.token.TokenKind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Map<String, TokenKind> KEYWORDS = Map.ofEntries(
        Map.entry("val",    TokenKind.VAL),
        Map.entry("var",    TokenKind.VAR),
        Map.entry("return", TokenKind.RETURN)
    );

    private static final Map<String, TokenKind> FIXEDTOKENS = buildFixedTokens();

    private static Map<String, TokenKind> buildFixedTokens() {
        Map<String, TokenKind> base = Map.ofEntries(
                // operators
                Map.entry("+",  TokenKind.PLUS),
                Map.entry("-",  TokenKind.MINUS),
                Map.entry("*",  TokenKind.MULT),
                Map.entry("/",  TokenKind.DIV),
                Map.entry("=",  TokenKind.ASSIGN),

                // punctuation
                Map.entry("(",  TokenKind.LPAREN),
                Map.entry(")",  TokenKind.RPAREN),
                Map.entry(";",  TokenKind.SEMI),

                // comments
                Map.entry("//", TokenKind.SLCOMMENT),
                Map.entry("/*", TokenKind.MLCOMMENT)
        );

        Map<String, TokenKind> result = new HashMap<>(base);

        for (String s : base.keySet()) {
            if (s.length() > 1) {
                for (int i = 1; i < s.length(); i++) {
                    String prefix = s.substring(0, i);
                    result.putIfAbsent(prefix, TokenKind.UNFINISHED);
                }
            }
        }

        return Map.copyOf(result);
    }
    
    // --- Char Functions ---

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
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
        return pos < src.length() ? src.charAt(pos) : 0;
    }

    private char peekAt(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got " + n);
        }

        return pos + n < src.length() ? src.charAt(pos + n) : 0;
    }


    private char step() {
        char c = peek();
        if (c == 0) return 0;
        if (c == '\n') {
            line++;
            col = 1;
        } else {
            col++;
        }
        pos++;
        return c;
    }

    private void skip(int n) {
        for (int i = 0; i < n; i++) {
            if (peek() == 0) {
                break;
            }
            step();
        }
    }

    private void skipWhitespace() {
        while (isWhitespace(peek())) {
            step();
        }
    }

    private void skipSLComment() {
        while (peek() != '\n' && peek() != 0) {
            step();
        }
    }

    private boolean skipMLComment() {
        while (peek() != 0) {
            if (step() == '*' && peek() == '/') {
                step();
                return true;
            }
        }

        return false;
    }

    private String readIdent() {
        if (!isLetter(peek()))
            return "";

        StringBuilder sb = new StringBuilder();
        char c;
        while (isIdentPart(c = peek())) {
            sb.append(c);
            step();
        }
        return sb.toString();
    }

    private String readInt() {
        if (!isDigit(peek()))
            return "";

        StringBuilder sb = new StringBuilder();
        char c;
        while (isDigit(c = peek())) {
            sb.append(c);
            step();
        }
        return sb.toString();
    }

    private Token getToken() {
        skipWhitespace();

        int startLine = line;
        int startCol = col;

        if (peek() == 0) {
            return new Token(TokenKind.EOF, "", startLine, startCol);
        }

        if (isLetter(peek())) {
            String v = readIdent();
            TokenKind kind = KEYWORDS.getOrDefault(v, TokenKind.IDENT);

            return new Token(kind, v, startLine, startCol);
        }

        if (isDigit(peek())) {
            String v = readInt();
            return new Token(TokenKind.INT, v, startLine, startCol);
        }

        TokenKind kind = TokenKind.ERROR;
        String value = "";

        StringBuilder buf = new StringBuilder();
        TokenKind bufKind;

        while(true) {
            char c = peekAt(buf.length());
            if (c == 0) break;
            buf.append(c);
            bufKind = FIXEDTOKENS.getOrDefault(buf.toString(), TokenKind.ERROR);

            if (bufKind == TokenKind.ERROR) break;

            if (bufKind != TokenKind.UNFINISHED) {
                value = buf.toString();
                kind = bufKind;
            }
        }

        if (kind == TokenKind.ERROR) {
            return new Token(TokenKind.ERROR, String.valueOf(step()), startLine, startCol);
        }

        skip(value.length());

        return new Token(kind, value, startLine, startCol);
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (true) {
            Token token = getToken();

            switch (token.kind()) {
                case TokenKind.SLCOMMENT -> skipSLComment();
                case TokenKind.MLCOMMENT -> {
                    if (!skipMLComment()) {
                        token = new Token(TokenKind.ERROR,
                                "Unterminated multi-line comment",
                                token.line(),
                                token.column());
                        tokens.add(token);
                    }
                }
                case TokenKind.EOF -> {
                    tokens.add(token);
                    return tokens;
                }
                default -> tokens.add(token);
            }
        }
    }
}