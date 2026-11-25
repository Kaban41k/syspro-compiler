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
        final Map<String, Integer> functions;

        Ctx(Map<String, Integer> functions) {
            this.functions = functions;
            scopes.push(new HashMap<>());
        }

        Mut lookup(String name) {
            for (Map<String, Mut> s : scopes) {
                Mut m = s.get(name);
                if (m != null) return m;
            }
            return null;
        }

        boolean isDeclared(String name) { return lookup(name) != null; }
        boolean declaredInCurrentScope(String name) { return scopes.peek().containsKey(name); }
        void declare(String name, Mut mut) { scopes.peek().put(name, mut); }
        void push() { scopes.push(new HashMap<>()); }
        void pop()  { scopes.pop(); }
    }

    public static boolean hasErrors(ProgramNode program) {
        Map<String, Integer> functions = new HashMap<>();
        boolean hasMain = false;

        for (Node d : program.body()) {
            String name;
            int arity;
            if (d instanceof FuncDecl f) {
                name = f.name();
                arity = f.params().size();
                if (name.equals("main")) hasMain = true;
            } else if (d instanceof ExternDecl e) {
                name = e.name();
                arity = e.params().size();
            } else {
                continue;
            }
            if (functions.containsKey(name)) return true;
            functions.put(name, arity);
        }

        if (!hasMain) return true;

        for (Node d : program.body()) {
            if (d instanceof FuncDecl f) {
                if (checkFunc(f, functions)) return true;
            } else if (d instanceof ExternDecl e) {
                if (checkExtern(e)) return true;
            }
        }

        return false;
    }

    private static boolean checkFunc(FuncDecl f, Map<String, Integer> functions) {
        Ctx ctx = new Ctx(functions);
        for (Ident p : f.params()) {
            if (ctx.declaredInCurrentScope(p.name())) return true;
            ctx.declare(p.name(), Mut.VAL);
        }
        return hasErrors(f.body(), ctx);
    }

    private static boolean checkExtern(ExternDecl e) {
        Map<String, Boolean> seen = new HashMap<>();
        for (Ident p : e.params()) {
            if (seen.put(p.name(), true) != null) return true;
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
            case Return r -> hasErrors(r.value(), ctx);
            case ExprStmt e -> hasErrors(e.value(), ctx);
            case Block b -> {
                ctx.push();
                boolean bad = b.body().stream().anyMatch(st -> hasErrors(st, ctx));
                ctx.pop();
                yield bad;
            }
            case If i -> {
                if (hasErrors(i.cond(), ctx)) yield true;
                if (hasErrors(i.thenBranch(), ctx)) yield true;
                yield i.elseBranch() != null && hasErrors(i.elseBranch(), ctx);
            }
            case While w -> hasErrors(w.cond(), ctx) || hasErrors(w.body(), ctx);
            case Break ignored -> false;
            case Continue ignored -> false;
        };
    }

    private static boolean hasErrors(Expr e, Ctx ctx) {
        return switch (e) {
            case Ident id -> !ctx.isDeclared(id.name());
            case BinOp b -> hasErrors(b.left(), ctx) || hasErrors(b.right(), ctx);
            case Unary u -> hasErrors(u.operand(), ctx);
            case Call c -> {
                Integer arity = ctx.functions.get(c.callee().name());
                if (arity != null && arity != c.args().size()) yield true;
                for (Expr arg : c.args()) {
                    if (hasErrors(arg, ctx)) yield true;
                }
                yield false;
            }
            case IntLiteral ignored -> false;
            case BoolLiteral ignored -> false;
            case ErrorExpr ignored -> false;
        };
    }
}