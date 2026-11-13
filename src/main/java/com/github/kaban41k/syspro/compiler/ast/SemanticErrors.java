package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class SemanticErrors {

    private SemanticErrors() {}

    private static final class Ctx {
        final Deque<Map<String, Mut>> scopes = new ArrayDeque<>();
        boolean insideWhile = false;

        Ctx() {
            scopes.push(new HashMap<>());
        }

        Mut lookup(String name) {
            for (Map<String, Mut> s : scopes) {
                Mut m = s.get(name);
                if (m != null) return m;
            }
            return null;
        }

        boolean isDeclared(String name) {
            return lookup(name) != null;
        }

        boolean declaredInCurrentScope(String name) {
            return scopes.peek().containsKey(name);
        }

        void declare(String name, Mut mut) {
            scopes.peek().put(name, mut);
        }

        void push() { scopes.push(new HashMap<>()); }
        void pop()  { scopes.pop(); }
    }

    public static boolean hasErrors(ProgramNode program) {
        Ctx ctx = new Ctx();
        for (Stmt s : program.body()) {
            if (hasErrors(s, ctx)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasErrors(Stmt s, Ctx ctx) {
        return switch (s) {
            case Declare d -> {
                if (hasErrors(d.init(), ctx)) yield true;
                if (ctx.declaredInCurrentScope(d.name())) yield true;
                ctx.declare(d.name(), d.mut());
                yield false;
            }
            case Assign a -> {
                if (hasErrors(a.target(), ctx)) yield true;
                if (ctx.lookup(a.target().name()) == Mut.VAL) yield true;
                yield hasErrors(a.value(), ctx);
            }
            case Return r      -> hasErrors(r.value(), ctx);
            case ExprStmt e    -> hasErrors(e.value(), ctx);
            case Block b       -> {
                ctx.push();
                boolean bad = b.body().stream().anyMatch(st -> hasErrors(st, ctx));
                ctx.pop();
                yield bad;
            }
            case If i          -> {
                if (hasErrors(i.cond(), ctx)) yield true;
                if (hasErrors(i.thenBranch(), ctx)) yield true;
                yield i.elseBranch() != null && hasErrors(i.elseBranch(), ctx);
            }
            case While w       -> {
                if (hasErrors(w.cond(), ctx)) yield true;
                boolean old = ctx.insideWhile;
                ctx.insideWhile = true;
                boolean bad = hasErrors(w.body(), ctx);
                ctx.insideWhile = old;
                yield bad;
            }
            case Break ignored    -> false;
            case Continue ignored -> false;
        };
    }

    private static boolean hasErrors(Expr e, Ctx ctx) {
        return switch (e) {
            case Ident id      -> !ctx.isDeclared(id.name());
            case BinOp b       -> hasErrors(b.left(), ctx) || hasErrors(b.right(), ctx);
            case Unary u       -> hasErrors(u.operand(), ctx);
            case IntLiteral ignored  -> false;
            case BoolLiteral ignored -> false;
            case ErrorExpr ignored   -> false;
        };
    }
}