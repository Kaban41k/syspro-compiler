package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.ast.Op;
import com.github.kaban41k.syspro.compiler.common.Position;

public record BinOp(
        Position pos,
        Op op,
        Expr left,
        Expr right
) implements Expr {
}