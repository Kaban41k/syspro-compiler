package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.ast.Node;

public sealed interface Expr extends Node permits
        BinOp, BoolLiteral, ErrorExpr, Ident, IntLiteral, Unary {
}