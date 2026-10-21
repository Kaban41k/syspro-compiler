package com.github.kaban41k.syspro.compiler.token;

import com.github.kaban41k.syspro.compiler.common.Position;

public record Token(TokenKind kind, String value, Position pos) {}

