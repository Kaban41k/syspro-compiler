package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

public final class AstErrors {

    private AstErrors() {}

    public static boolean hasErrors(Node node) {
        return switch (node) {
            case ProgramNode p -> programHasErrors(p);
            case Declare d     -> hasErrors(d.init());
            case Return r      -> hasErrors(r.value());
            case Assign a      -> hasErrors(a.target()) || hasErrors(a.value());
            case ExprStmt e    -> hasErrors(e.value());
            case BinOp b       -> hasErrors(b.left()) || hasErrors(b.right());
            case Unary u       -> hasErrors(u.operand());
            case ErrorExpr ee  -> true;
            default -> false;
        };
    }

    private static boolean programHasErrors(ProgramNode p) {
        if (p.body().isEmpty()) {
            return false;
        }
        if (!(p.body().getLast() instanceof Return)) {
            return true;
        }
        return p.body().stream().anyMatch(AstErrors::hasErrors);
    }
}