package com.github.kaban41k.syspro.compiler.lexer;

import com.github.kaban41k.syspro.compiler.token.Token;
import com.github.kaban41k.syspro.compiler.token.TokenKind;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JsonizerTest {
    @Test
    void testSingleEOFToken() {
        String expected = """
                          [
                          {"kind": "EOF", "value": "", "line": 1, "column": 1}
                          ]
                          """;

        List<Token> tokens = List.of(
                new Token(TokenKind.EOF, "", 1, 1)
        );

        assertEquals(expected, Jsonizer.tokensToJson(tokens));
    }

    @Test
    void testSimpleAssignment() {
        String expected = """
                          [
                          {"kind": "VAR", "value": "var", "line": 1, "column": 1},
                          {"kind": "IDENT", "value": "x", "line": 1, "column": 5},
                          {"kind": "ASSIGN", "value": "=", "line": 1, "column": 7},
                          {"kind": "INT", "value": "10", "line": 1, "column": 9},
                          {"kind": "SEMI", "value": ";", "line": 1, "column": 11},
                          {"kind": "EOF", "value": "", "line": 2, "column": 1}
                          ]
                          """;

        List<Token> tokens = List.of(
                new Token(TokenKind.VAR,    "var", 1, 1),
                new Token(TokenKind.IDENT,  "x",   1, 5),
                new Token(TokenKind.ASSIGN, "=",   1, 7),
                new Token(TokenKind.INT,    "10",  1, 9),
                new Token(TokenKind.SEMI,   ";",   1, 11),
                new Token(TokenKind.EOF,    "",    2, 1)
        );

        assertEquals(expected, Jsonizer.tokensToJson(tokens));
    }
}
