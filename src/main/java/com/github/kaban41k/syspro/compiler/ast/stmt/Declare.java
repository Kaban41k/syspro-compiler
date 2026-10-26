package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.ast.Mut;
import com.github.kaban41k.syspro.compiler.ast.expr.Expr;
import com.github.kaban41k.syspro.compiler.common.Position;

public record Declare(
        Position pos,
        Mut mut,
        String name,
        Expr init
) implements Stmt {
}