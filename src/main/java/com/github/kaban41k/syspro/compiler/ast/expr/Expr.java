package com.github.kaban41k.syspro.compiler.ast.expr;

import com.github.kaban41k.syspro.compiler.ast.Node;

public sealed interface Expr extends Node permits BinOp, ErrorExpr, Ident, IntLiteral, Unary {
}