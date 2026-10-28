package com.github.kaban41k.syspro.compiler.codegen;

import com.github.kaban41k.syspro.compiler.ast.ProgramNode;
import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

public class LlvmGenerator {

    private final StringBuilder out = new StringBuilder();
    private final Map<String, String> locals = new HashMap<>();
    private final Set<String> usedNames = new HashSet<>();
    private boolean terminated = false;

    public String generate(ProgramNode program) {
        out.append("; ModuleID = 'spl'\n");
        out.append("source_filename = \"spl\"\n");
        out.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128\"\n");
        out.append("target triple = \"x86_64-unknown-linux-gnu\"\n");
        out.append("\n");
        out.append("define i64 @main() {\n");
        out.append("entry:\n");

        for (Stmt s : program.body()) {
            if (terminated) break;
            genStmt(s);
        }

        if (!terminated) {
            emit("ret i64 0");
        }

        out.append("}\n");

        return out.toString();
    }

    private void emit(String line) {
        out.append("  ").append(line).append("\n");
    }

    private String uniqueName(String base) {
        String name = "%" + base;

        if (usedNames.add(name)) return name;

        int n = 1;
        while (!usedNames.add(name + n)) n++;

        return name + n;
    }

    private void genStmt(Stmt s) {
        switch (s) {
            case Declare d -> {
                String addr = uniqueName(d.name());
                emit(addr + " = alloca i64, align 8");

                String val = genExpr(d.init());
                emit("store i64 " + val + ", ptr " + addr + ", align 8");

                locals.put(d.name(), addr);
            }
            case Assign a -> {
                String val = genExpr(a.value());
                String addr = locals.get(a.target().name());
                Objects.requireNonNull(addr, "undeclared variable: " + a.target().name());
                emit("store i64 " + val + ", ptr " + addr + ", align 8");
            }
            case Return r -> {
                String val = genExpr(r.value());
                emit("ret i64 " + val);
                terminated = true;
            }
            case ExprStmt e -> genExpr(e.value());
            default -> throw new IllegalStateException("unknown stmt: " + s);
        }
    }

    private String genExpr(Expr e) {
        return switch (e) {
            case IntLiteral il -> String.valueOf(il.value());
            case Ident ident -> {
                String addr = locals.get(ident.name());
                Objects.requireNonNull(addr, "undeclared variable: " + ident.name());
                String name = uniqueName(ident.name());
                emit(name + " = load i64, ptr " + addr + ", align 8");
                yield name;
            }
            case BinOp binOp -> {
                String l = genExpr(binOp.left());
                String r = genExpr(binOp.right());
                String op = switch (binOp.op()) {
                    case PLUS -> "add";
                    case MINUS -> "sub";
                    case MULT -> "mul";
                    case DIV -> "sdiv";
                    default -> throw new IllegalStateException(":( bad op: " + binOp.op());
                };
                String base = switch (binOp.op()) {
                    case PLUS -> "addtmp";
                    case MINUS -> "subtmp";
                    case MULT -> "multmp";
                    case DIV -> "divtmp";
                    default -> throw new IllegalStateException(":( bad op: " + binOp.op());
                };
                String name = uniqueName(base);
                emit(name + " = " + op + " i64 " + l + ", " + r);
                yield name;
            }
            case Unary u -> {
                String v = genExpr(u.operand());
                String name = uniqueName("negtmp");
                emit(name + " = sub i64 0, " + v);
                yield name;
            }
            case ErrorExpr ee -> throw new IllegalStateException("codegen on error: " + ee.message());
        };
    }
}