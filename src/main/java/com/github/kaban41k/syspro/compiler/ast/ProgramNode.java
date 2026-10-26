package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.stmt.Stmt;
import com.github.kaban41k.syspro.compiler.common.Position;

import java.util.List;

public record ProgramNode(
        Position pos,
        List<Stmt> body
) implements Node {
}