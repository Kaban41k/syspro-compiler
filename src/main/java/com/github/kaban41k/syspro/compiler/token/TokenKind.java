package com.github.kaban41k.syspro.compiler.token;

public enum TokenKind {
    // literals
    IDENT, INT, TRUE, FALSE,

    // keywords
    VAL, VAR, RETURN, IF, ELSE, WHILE, BREAK, CONTINUE,

    // operators
    PLUS, MINUS, MULT, DIV, ASSIGN,
    EQ, NE, LT, GT, LE, GE,
    AND, OR, NOT,

    // punctuation
    LPAREN, RPAREN, LBRACE, RBRACE, SEMI,

    // special
    EOF, ERROR,
    UNFINISHED, SLCOMMENT, MLCOMMENT
}