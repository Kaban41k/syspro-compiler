package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.common.Position;
import java.util.List;

public record Call(
        Position pos,
        Ident callee,
        List<Expr> args
) implements Expr {
}