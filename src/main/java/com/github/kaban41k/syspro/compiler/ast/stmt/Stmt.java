package com.github.kaban41k.syspro.compiler.ast.stmt;

import com.github.kaban41k.syspro.compiler.ast.Node;

public sealed interface Stmt extends Node permits
        Declare,
        Return,
        Assign,
        ExprStmt {
}