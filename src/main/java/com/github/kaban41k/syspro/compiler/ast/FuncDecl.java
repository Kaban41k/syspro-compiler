package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.Ident;
import com.github.kaban41k.syspro.compiler.ast.stmt.Block;
import com.github.kaban41k.syspro.compiler.common.Position;
import java.util.List;

public record FuncDecl(
        Position pos,
        String name,
        List<Ident> params,
        Block body
) implements TopDecl {
}