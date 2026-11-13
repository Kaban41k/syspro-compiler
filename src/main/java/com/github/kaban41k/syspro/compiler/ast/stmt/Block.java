package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.common.Position;

import java.util.List;

public record Block(
        Position pos,
        List<Stmt> body
) implements Stmt {
}