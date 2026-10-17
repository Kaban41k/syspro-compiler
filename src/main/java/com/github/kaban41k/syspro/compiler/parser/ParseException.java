package com.github.kaban41k.syspro.compiler.parser;

import com.github.kaban41k.syspro.compiler.common.Position;

public class ParseException extends RuntimeException {
    private final Position pos;

    public ParseException(String msg, Position pos) {
        super(msg + " at " + pos.line() + ":" + pos.column());
        this.pos = pos;
    }

    public Position pos() {
        return pos;
    }
}