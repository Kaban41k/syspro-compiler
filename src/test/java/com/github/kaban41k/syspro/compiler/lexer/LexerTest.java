package com.github.kaban41k.syspro.compiler.lexer;

import com.github.kaban41k.syspro.compiler.token.Token;
import com.github.kaban41k.syspro.compiler.token.TokenKind;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {
    @Test
    void testSingleIdent() {
        List<Token> expected = List.of(
                new Token(TokenKind.IDENT, "j", 1, 1),
                new Token(TokenKind.EOF,   "",  1, 2)
        );

        Lexer l = new Lexer("j");
        List<Token> tokens = l.tokenize();

        assertEquals(expected, tokens);
    }

    @Test
    void testSingleCommentLine() {
        List<Token> expected = List.of(
                new Token(TokenKind.EOF, "", 2, 1)
        );

        Lexer l = new Lexer(
            """
            // it's a comment
            """
        );
        List<Token> tokens = l.tokenize();

        assertEquals(expected, tokens);
    }

    @Test
    void testMultiCommentLine() {
        List<Token> expected = List.of(
                new Token(TokenKind.EOF, "", 3, 1)
        );

        Lexer l = new Lexer(
            """
            /* it's a comment
            and this*/
            """
        );
        List<Token> tokens = l.tokenize();

        assertEquals(expected, tokens);
    }

    @Test
    void testUnterminatedMultiCommentLine() {
        List<Token> expected = List.of(
                new Token(TokenKind.ERROR, "Unterminated multi-line comment", 1, 1),
                new Token(TokenKind.EOF, "", 3, 1)
        );

        Lexer l = new Lexer(
                """
                /* it's a comment
                and this
                """
        );
        List<Token> tokens = l.tokenize();

        assertEquals(expected, tokens);
    }

    @Test
    void testSimpleAssignment() {
        List<Token> expected = List.of(
                new Token(TokenKind.VAR,    "var", 1, 1),
                new Token(TokenKind.IDENT,  "x",   1, 5),
                new Token(TokenKind.ASSIGN, "=",   1, 7),
                new Token(TokenKind.INT,    "10",  1, 9),
                new Token(TokenKind.SEMI,   ";",   1, 11),
                new Token(TokenKind.EOF,    "",    2, 1)
        );

        Lexer l = new Lexer(
                """
                var x = 10;
                """
        );
        List<Token> tokens = l.tokenize();

        assertEquals(expected, tokens);
    }
}
