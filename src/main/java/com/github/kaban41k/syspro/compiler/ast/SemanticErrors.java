package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

import java.util.HashMap;
import java.util.Map;

public final class SemanticErrors {

    private SemanticErrors() {}

    public static boolean hasErrors(ProgramNode program) {
        Map<String, Mut> declared = new HashMap<>();

        for (Stmt s : program.body()) {
            if (hasErrors(s, declared)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasErrors(Stmt s, Map<String, Mut> declared) {
        return switch (s) {
            case Declare d -> {
                if (hasErrors(d.init(), declared)) {
                    yield true;
                }
                if (declared.containsKey(d.name())) {
                    yield true;
                }
                declared.put(d.name(), d.mut());
                yield false;
            }
            case Assign a -> {
                if (hasErrors(a.target(), declared)) {
                    yield true;
                }
                Mut mut = declared.get(a.target().name());
                if (mut == Mut.VAL) {
                    yield true;
                }
                yield hasErrors(a.value(), declared);
            }
            case Return r -> hasErrors(r.value(), declared);
            case ExprStmt e -> hasErrors(e.value(), declared);
        };
    }

    private static boolean hasErrors(Expr e, Map<String, Mut> declared) {
        return switch (e) {
            case Ident id -> !declared.containsKey(id.name());
            case BinOp b -> hasErrors(b.left(), declared) || hasErrors(b.right(), declared);
            case Unary u -> hasErrors(u.operand(), declared);
            case IntLiteral ignored -> false;
            case ErrorExpr ignored -> false;
        };
    }
}