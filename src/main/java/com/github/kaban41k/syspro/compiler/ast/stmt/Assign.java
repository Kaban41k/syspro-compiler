package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.ast.expr.Expr;
import com.github.kaban41k.syspro.compiler.ast.expr.Ident;
import com.github.kaban41k.syspro.compiler.common.Position;

public record Assign(
        Position pos,
        Ident target,
        Expr value
) implements Stmt {
}