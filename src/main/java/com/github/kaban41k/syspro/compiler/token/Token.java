package com.github.kaban41k.syspro.compiler.token;

public record Token(TokenKind kind, String value, int line, int column) {}

