package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.common.Position;

public record IntLiteral(
        Position pos,
        long value
) implements Expr {
}