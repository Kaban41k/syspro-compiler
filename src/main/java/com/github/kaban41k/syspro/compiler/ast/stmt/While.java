package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.ast.expr.Expr;
import com.github.kaban41k.syspro.compiler.common.Position;

public record While(
        Position pos,
        Expr cond,
        Stmt body
) implements Stmt {
}