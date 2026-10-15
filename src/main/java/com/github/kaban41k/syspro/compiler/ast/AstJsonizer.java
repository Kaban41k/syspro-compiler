package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.Assign;
import com.github.kaban41k.syspro.compiler.ast.stmt.Declare;
import com.github.kaban41k.syspro.compiler.ast.stmt.ExprStmt;
import com.github.kaban41k.syspro.compiler.ast.stmt.Return;

import com.github.kaban41k.syspro.compiler.common.Position;

public final class AstJsonizer {

    private final StringBuilder sb = new StringBuilder();
    private int depth = 0;
    private boolean hasFields = false;
    private final java.util.Deque<Boolean> fieldStack = new java.util.ArrayDeque<>();

    private AstJsonizer() {}

    public static String toJson(Node node) {
        AstJsonizer w = new AstJsonizer();
        w.node(node);
        return w.sb.toString();
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default   -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private void node(Node n) {
        switch (n) {
            case ProgramNode p -> program(p);
            case Declare d     -> declare(d);
            case Return r      -> returnStmt(r);
            case Assign a      -> assign(a);
            case ExprStmt e    -> exprStmt(e);
            case IntLiteral i  -> intLiteral(i);
            case Ident id      -> ident(id);
            case BinOp b       -> binOp(b);
            case Unary u       -> unaryOp(u);
            case ErrorExpr e   -> errorExpr(e);
            default -> throw new IllegalStateException("unknown node: " + n);
        }
    }

    private void openObject(Position pos, String kind) {
        sb.append("{\n");
        depth++;
        fieldStack.push(hasFields);
        hasFields = false;
        fieldInt("line", pos.line());
        fieldInt("column", pos.column());
        fieldString("kind", kind);
    }

    private void closeObject() {
        sb.append("\n");
        depth--;
        indent();
        sb.append("}");
        hasFields = fieldStack.pop();
    }

    private void program(ProgramNode p) {
        openObject(p.pos(), "Program");
        comma();
        indent();
        sb.append('"').append("body").append("\": [");
        if (!p.body().isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < p.body().size(); i++) {
                indent();
                node(p.body().get(i));
                if (i < p.body().size() - 1) sb.append(",");
                sb.append("\n");
            }
            indent();
        }
        sb.append("]");
        closeObject();
    }

    private void declare(Declare d) {
        openObject(d.pos(), "Declare");
        fieldString("mut", d.mut() == Mut.VAL ? "val" : "var");
        fieldString("name", d.name());
        fieldNode("init", d.init());
        closeObject();
    }

    private void returnStmt(Return r) {
        openObject(r.pos(), "Return");
        fieldNode("value", r.value());
        closeObject();
    }

    private void assign(Assign a) {
        openObject(a.pos(), "Assign");
        fieldNode("target", a.target());
        fieldNode("value", a.value());
        closeObject();
    }

    private void exprStmt(ExprStmt e) {
        openObject(e.pos(), "ExprStmt");
        fieldNode("value", e.value());
        closeObject();
    }

    private void intLiteral(IntLiteral i) {
        openObject(i.pos(), "IntLiteral");
        fieldInt("value", i.value());
        closeObject();
    }

    private void ident(Ident id) {
        openObject(id.pos(), "Ident");
        fieldString("name", id.name());
        closeObject();
    }

    private void binOp(BinOp b) {
        openObject(b.pos(), "BinOp");
        fieldString("op", b.op().text());
        fieldNode("left", b.left());
        fieldNode("right", b.right());
        closeObject();
    }

    private void unaryOp(Unary u) {
        openObject(u.pos(), "Unary");
        fieldString("op", u.op().text());
        fieldNode("operand", u.operand());
        closeObject();
    }

    private void errorExpr(ErrorExpr e) {
        openObject(e.pos(), "Error");
        fieldString("message", e.message());
        closeObject();
    }

    private void fieldInt(String s, int value) {
        comma();
        indent();
        sb.append('"').append(s).append("\": ").append(value);
    }

    private void fieldString(String s, String value) {
        comma();
        indent();
        sb.append('"').append(s).append("\": \"").append(escape(value)).append('"');
    }

    private void fieldNode(String s, Node value) {
        comma();
        indent();
        sb.append('"').append(s).append("\": ");
        node(value);
    }

    private void comma() {
        if (hasFields) {
            sb.append(",\n");
        }
        hasFields = true;
    }

    private void indent() {
        sb.repeat("  ", depth);
    }
}