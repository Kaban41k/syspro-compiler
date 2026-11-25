package com.github.kaban41k.syspro.compiler.codegen;

import com.github.kaban41k.syspro.compiler.ast.*;
import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;

import java.util.*;

public class LlvmGenerator {

    private final StringBuilder out = new StringBuilder();
    private final Map<String, String> locals = new HashMap<>();
    private final Set<String> usedNames = new HashSet<>();
    private final Deque<LoopCtx> loops = new ArrayDeque<>();
    private final Set<String> knownFunctions = new HashSet<>();
    private final Set<String> emittedDeclares = new HashSet<>();
    private final Set<String> pendingDeclares = new LinkedHashSet<>();
    private int labelCounter = 0;
    private String currentBlock = "entry";
    private boolean terminated = false;

    private record LoopCtx(String breakLabel, String continueLabel) {}

    public String generate(ProgramNode program) {
        out.append("; ModuleID = 'spl'\n");
        out.append("source_filename = \"spl\"\n");
        out.append("target datalayout = \"e-m:e-p270:32:32-p271:32:32-p272:64:64-i64:64-i128:128-f80:128-n8:16:32:64-S128\"\n");
        out.append("target triple = \"x86_64-unknown-linux-gnu\"\n");
        out.append("\n");

        for (Node d : program.body()) {
            if (d instanceof ExternDecl e) {
                knownFunctions.add(e.name());
                genExternDecl(e);
            } else if (d instanceof FuncDecl f) {
                knownFunctions.add(f.name());
            }
        }

        for (Node d : program.body()) {
            if (d instanceof FuncDecl f) {
                genFuncDecl(f);
            }
        }

        return out.toString();
    }

    private void genExternDecl(ExternDecl e) {
        out.append("declare i64 @").append(e.name()).append("(");
        for (int i = 0; i < e.params().size(); i++) {
            if (i > 0) out.append(", ");
            out.append("i64");
        }
        out.append(")\n");
    }

    private void genFuncDecl(FuncDecl f) {
        resetForFunction();

        out.append("\ndefine i64 @").append(f.name()).append("(");
        for (int i = 0; i < f.params().size(); i++) {
            if (i > 0) out.append(", ");
            out.append("i64 %").append(f.params().get(i).name()).append(".arg");
        }
        out.append(") {\nentry:\n");

        for (Ident p : f.params()) {
            String argName = "%" + p.name() + ".arg";
            String addr = uniqueName(p.name());
            emit(addr + " = alloca i64, align 8");
            emit("store i64 " + argName + ", ptr " + addr + ", align 8");
            locals.put(p.name(), addr);
        }

        genStmt(f.body());

        if (!terminated) {
            emit("ret i64 0");
        }
        out.append("}\n");

        for (String name : pendingDeclares) {
            if (emittedDeclares.add(name)) {
                out.append("\ndeclare i64 @").append(name).append("()\n");
            }
        }
        pendingDeclares.clear();
    }

    private void resetForFunction() {
        locals.clear();
        usedNames.clear();
        loops.clear();
        labelCounter = 0;
        currentBlock = "entry";
        terminated = false;
    }

    private void emit(String line) {
        out.append("  ").append(line).append("\n");
    }

    private void emitLabel(String label) {
        out.append(label).append(":\n");
        currentBlock = label;
        terminated = false;
    }

    private String newLabel(String base) {
        return base + "." + (labelCounter++);
    }

    private String uniqueName(String base) {
        String name = "%" + base;
        if (usedNames.add(name)) return name;
        int n = 1;
        while (!usedNames.add(name + n)) n++;
        return name + n;
    }

    private String toBool(String val) {
        String tmp = uniqueName("cond");
        emit(tmp + " = icmp ne i64 " + val + ", 0");
        return tmp;
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
            case Block b -> {
                Map<String, String> saved = new HashMap<>(locals);
                for (Stmt st : b.body()) {
                    if (terminated) break;
                    genStmt(st);
                }
                locals.clear();
                locals.putAll(saved);
            }
            case If i -> genIf(i);
            case While w -> genWhile(w);
            case Break b -> {
                if (loops.isEmpty()) throw new IllegalStateException("break outside loop");
                emit("br label %" + loops.peek().breakLabel());
                terminated = true;
            }
            case Continue c -> {
                if (loops.isEmpty()) throw new IllegalStateException("continue outside loop");
                emit("br label %" + loops.peek().continueLabel());
                terminated = true;
            }
        }
    }

    private void genIf(If i) {
        String cond = genExpr(i.cond());
        String condBool = toBool(cond);
        String thenLabel = newLabel("if.then");
        String endLabel = newLabel("if.end");
        String elseLabel = i.elseBranch() != null ? newLabel("if.else") : null;

        if (elseLabel != null) {
            emit("br i1 " + condBool + ", label %" + thenLabel + ", label %" + elseLabel);
        } else {
            emit("br i1 " + condBool + ", label %" + thenLabel + ", label %" + endLabel);
        }
        terminated = true;

        emitLabel(thenLabel);
        genStmt(i.thenBranch());
        boolean thenTerminated = terminated;
        if (!thenTerminated) emit("br label %" + endLabel);

        boolean elseTerminated = true;
        if (elseLabel != null) {
            emitLabel(elseLabel);
            genStmt(i.elseBranch());
            elseTerminated = terminated;
            if (!elseTerminated) emit("br label %" + endLabel);
        }

        if (thenTerminated && elseTerminated && elseLabel != null) {
            terminated = true;
            return;
        }

        emitLabel(endLabel);
    }

    private void genWhile(While w) {
        String headLabel = newLabel("while.head");
        String bodyLabel = newLabel("while.body");
        String exitLabel = newLabel("while.exit");

        emit("br label %" + headLabel);
        terminated = true;

        emitLabel(headLabel);
        String cond = genExpr(w.cond());
        String condBool = toBool(cond);
        emit("br i1 " + condBool + ", label %" + bodyLabel + ", label %" + exitLabel);
        terminated = true;

        emitLabel(bodyLabel);
        loops.push(new LoopCtx(exitLabel, headLabel));
        genStmt(w.body());
        loops.pop();
        if (!terminated) emit("br label %" + headLabel);
        terminated = true;

        emitLabel(exitLabel);
    }

    private String genExpr(Expr e) {
        return switch (e) {
            case IntLiteral il -> String.valueOf(il.value());
            case BoolLiteral bl -> bl.value() ? "1" : "0";
            case Ident ident -> {
                String addr = locals.get(ident.name());
                Objects.requireNonNull(addr, "undeclared variable: " + ident.name());
                String name = uniqueName(ident.name());
                emit(name + " = load i64, ptr " + addr + ", align 8");
                yield name;
            }
            case BinOp binOp -> genBinOp(binOp);
            case Unary u -> genUnary(u);
            case Call c -> {
                List<String> argVals = new ArrayList<>();
                for (Expr a : c.args()) argVals.add(genExpr(a));
                StringBuilder sb = new StringBuilder();
                sb.append("call i64 @").append(c.callee().name()).append("(");
                for (int i = 0; i < argVals.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append("i64 ").append(argVals.get(i));
                }
                sb.append(")");
                String name = uniqueName("calltmp");
                emit(name + " = " + sb);

                String callee = c.callee().name();
                if (!knownFunctions.contains(callee) && !emittedDeclares.contains(callee)) {
                    pendingDeclares.add(callee);
                }
                yield name;
            }
            case ErrorExpr ee -> throw new IllegalStateException("codegen on error: " + ee.message());
        };
    }

    private String genBinOp(BinOp b) {
        Op op = b.op();
        if (op == Op.AND) return genAnd(b);
        if (op == Op.OR)  return genOr(b);
        if (op == Op.EQ || op == Op.NE || op == Op.LT || op == Op.GT || op == Op.LE || op == Op.GE) {
            String l = genExpr(b.left());
            String r = genExpr(b.right());
            String cmpOp = switch (op) {
                case EQ -> "eq";
                case NE -> "ne";
                case LT -> "slt";
                case GT -> "sgt";
                case LE -> "sle";
                case GE -> "sge";
                default -> throw new IllegalStateException("bad cmp: " + op);
            };
            String cmp = uniqueName("cmptmp");
            emit(cmp + " = icmp " + cmpOp + " i64 " + l + ", " + r);
            String result = uniqueName("booltmp");
            emit(result + " = zext i1 " + cmp + " to i64");
            return result;
        }
        String l = genExpr(b.left());
        String r = genExpr(b.right());
        String irOp = switch (op) {
            case PLUS -> "add";
            case MINUS -> "sub";
            case MULT -> "mul";
            case DIV -> "sdiv";
            default -> throw new IllegalStateException("bad op: " + op);
        };
        String base = switch (op) {
            case PLUS -> "addtmp";
            case MINUS -> "subtmp";
            case MULT -> "multmp";
            case DIV -> "divtmp";
            default -> throw new IllegalStateException("bad op: " + op);
        };
        String name = uniqueName(base);
        emit(name + " = " + irOp + " i64 " + l + ", " + r);
        return name;
    }

    private String genAnd(BinOp b) {
        String l = genExpr(b.left());
        String lBool = toBool(l);
        String fromBlock = currentBlock;
        String rhsLabel = newLabel("and.rhs");
        String endLabel = newLabel("and.end");

        emit("br i1 " + lBool + ", label %" + rhsLabel + ", label %" + endLabel);
        terminated = true;

        emitLabel(rhsLabel);
        String r = genExpr(b.right());
        String rBool = toBool(r);
        emit("br label %" + endLabel);
        terminated = true;

        emitLabel(endLabel);
        String phi = uniqueName("andtmp");
        emit(phi + " = phi i1 [ false, %" + fromBlock + " ], [ " + rBool + ", %" + rhsLabel + " ]");
        String result = uniqueName("andtmp");
        emit(result + " = zext i1 " + phi + " to i64");
        return result;
    }

    private String genOr(BinOp b) {
        String l = genExpr(b.left());
        String lBool = toBool(l);
        String fromBlock = currentBlock;
        String rhsLabel = newLabel("or.rhs");
        String endLabel = newLabel("or.end");

        emit("br i1 " + lBool + ", label %" + endLabel + ", label %" + rhsLabel);
        terminated = true;

        emitLabel(rhsLabel);
        String r = genExpr(b.right());
        String rBool = toBool(r);
        emit("br label %" + endLabel);
        terminated = true;

        emitLabel(endLabel);
        String phi = uniqueName("ortmp");
        emit(phi + " = phi i1 [ true, %" + fromBlock + " ], [ " + rBool + ", %" + rhsLabel + " ]");
        String result = uniqueName("ortmp");
        emit(result + " = zext i1 " + phi + " to i64");
        return result;
    }

    private String genUnary(Unary u) {
        if (u.op() == Op.MINUS) {
            String v = genExpr(u.operand());
            String name = uniqueName("negtmp");
            emit(name + " = sub i64 0, " + v);
            return name;
        }
        if (u.op() == Op.NOT) {
            String v = genExpr(u.operand());
            String cmp = uniqueName("nottmp");
            emit(cmp + " = icmp eq i64 " + v + ", 0");
            String result = uniqueName("booltmp");
            emit(result + " = zext i1 " + cmp + " to i64");
            return result;
        }
        throw new IllegalStateException("bad unary op: " + u.op());
    }
}