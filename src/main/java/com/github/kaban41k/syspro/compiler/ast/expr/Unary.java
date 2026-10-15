package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.ast.Op;
import com.github.kaban41k.syspro.compiler.common.Position;

public record Unary(
        Position pos,
        Op op,
        Expr operand
) implements Expr {
}