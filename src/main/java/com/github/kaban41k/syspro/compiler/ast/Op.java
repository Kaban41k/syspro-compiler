package com.github.kaban41k.syspro.compiler.ast;

public enum Op {
    PLUS ("+"),
    MINUS("-"),
    MULT ("*"),
    DIV  ("/"),
    UNEXPECTED("UNEXPECTED");

    private final String text;

    Op(String text) {
        this.text = text;
    }

    public String text() {
        return text;
    }
}