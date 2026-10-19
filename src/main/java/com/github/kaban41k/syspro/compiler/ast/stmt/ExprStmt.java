package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.ast.expr.Expr;
import com.github.kaban41k.syspro.compiler.common.Position;

public record ExprStmt(
        Position pos,
        Expr value
) implements Stmt {
}