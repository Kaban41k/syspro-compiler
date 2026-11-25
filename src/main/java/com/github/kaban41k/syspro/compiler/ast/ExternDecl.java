package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.Ident;
import com.github.kaban41k.syspro.compiler.common.Position;
import java.util.List;

public record ExternDecl(
        Position pos,
        String name,
        List<Ident> params
) implements TopDecl {
}