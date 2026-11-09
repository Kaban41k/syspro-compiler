package com.github.kaban41k.syspro.compiler.ast;

public enum Op {
    PLUS ("+"),
    MINUS("-"),
    MULT ("*"),
    DIV  ("/"),
    EQ   ("=="),
    NE   ("!="),
    LT   ("<"),
    GT   (">"),
    LE   ("<="),
    GE   (">="),
    AND  ("&&"),
    OR   ("||"),
    NOT  ("!"),
    UNEXPECTED("UNEXPECTED");

    private final String text;

    Op(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}