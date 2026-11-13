package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.common.Position;

public record Continue(
        Position pos
) implements Stmt {
}