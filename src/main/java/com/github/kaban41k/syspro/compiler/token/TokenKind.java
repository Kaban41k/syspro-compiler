package com.github.kaban41k.syspro.compiler.token;

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
    EOF, ERROR,
    UNFINISHED, SLCOMMENT, MLCOMMENT
}