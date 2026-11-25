package com.github.kaban41k.syspro.compiler.ast;

import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

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
            case BoolLiteral b -> boolLiteral(b);
            case Block b       -> block(b);
            case If i          -> ifStmt(i);
            case While w       -> whileStmt(w);
            case Break b       -> breakStmt(b);
            case Continue c    -> continueStmt(c);
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
        fieldArray("body", p.body());
        closeObject();
    }

    private void fieldBool(String s, boolean value) {
        comma();
        indent();
        sb.append('"').append(s).append("\": ").append(value);
    }

    private void fieldArray(String s, java.util.List<? extends Node> nodes) {
        comma();
        indent();
        sb.append('"').append(s).append("\": [");
        if (!nodes.isEmpty()) {
            sb.append("\n");
            for (int i = 0; i < nodes.size(); i++) {
                indent();
                node(nodes.get(i));
                if (i < nodes.size() - 1) sb.append(",");
                sb.append("\n");
            }
            indent();
        }
        sb.append("]");
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

    private void boolLiteral(BoolLiteral b) {
        openObject(b.pos(), "BoolLiteral");
        fieldBool("value", b.value());
        closeObject();
    }

    private void block(Block b) {
        openObject(b.pos(), "Block");
        fieldArray("body", b.body());
        closeObject();
    }

    private void ifStmt(If i) {
        openObject(i.pos(), "If");
        fieldNode("cond", i.cond());
        fieldNode("then", i.thenBranch());
        if (i.elseBranch() != null) {
            fieldNode("else", i.elseBranch());
        }
        closeObject();
    }

    private void whileStmt(While w) {
        openObject(w.pos(), "While");
        fieldNode("cond", w.cond());
        fieldNode("body", w.body());
        closeObject();
    }

    private void breakStmt(Break ignored) {
        openObject(ignored.pos(), "Break");
        closeObject();
    }

    private void continueStmt(Continue ignored) {
        openObject(ignored.pos(), "Continue");
        closeObject();
    }

    private void fieldInt(String s, long value) {
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