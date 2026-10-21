package com.github.kaban41k.syspro.compiler.parser;

import com.github.kaban41k.syspro.compiler.ast.Mut;
import com.github.kaban41k.syspro.compiler.ast.Op;
import com.github.kaban41k.syspro.compiler.ast.ProgramNode;
import com.github.kaban41k.syspro.compiler.ast.expr.*;
import com.github.kaban41k.syspro.compiler.ast.stmt.*;
import com.github.kaban41k.syspro.compiler.common.Position;
import com.github.kaban41k.syspro.compiler.token.Token;
import com.github.kaban41k.syspro.compiler.token.TokenKind;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Token> tokens;
    private final List<String> errors = new ArrayList<>();
    private int pos;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public ProgramNode parse() {
        return parseProgram();
    }

    public List<String> errors() {
        return errors;
    }

    private Token peek() {
        return tokens.get(pos);
    }

    private Token peekAt(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be >= 0, got " + n);
        }
        return pos + n < tokens.size() ? tokens.get(pos + n) : tokens.getLast();
    }

    private Token step() {
        Token t = peek();
        if (t.kind() != TokenKind.EOF) {
            pos++;
        }
        return t;
    }

    private Token previous() {
        if (pos <= 0) {
            throw new IllegalArgumentException("pos must be > 0");
        }
        return tokens.get(pos - 1);
    }

    private boolean check(TokenKind kind) {
        return peek().kind() == kind;
    }

    private boolean checkAt(TokenKind kind, int n) {
        return peekAt(n).kind() == kind;
    }

    private boolean matchStep(TokenKind... kinds) {
        for (TokenKind k : kinds) {
            if (check(k)) {
                step();
                return true;
            }
        }
        return false;
    }

    private Token expectStep(TokenKind kind) {
        if (!check(kind)) {
            Token t = peek();
            String value = "expected '" + kind + "', got '" + t.kind() + "' at " + t.pos().line() + ":" + t.pos().column();

            errors.add(value);

            return new Token(
                    TokenKind.ERROR,
                    value,
                    t.pos()
            );
        }

        return step();
    }

    private ParseException error(String msg) {
        return new ParseException(msg, peek().pos());
    }

    private Op toOp(Token t) {
        return switch (t.kind()) {
            case PLUS  -> Op.PLUS;
            case MINUS -> Op.MINUS;
            case MULT  -> Op.MULT;
            case DIV   -> Op.DIV;
            default    -> Op.UNEXPECTED;
        };
    }

    private ProgramNode parseProgram() {
        List<Stmt> body = new ArrayList<>();

        while (!check(TokenKind.EOF)) {
            body.add(parseStatement());
        }

        Token end = body.isEmpty() ? peek() : previous();

        return new ProgramNode(end.pos(), body);
    }

    private Stmt parseStatement() {
        if (check(TokenKind.RETURN)) {
            return parseReturn();
        }
        if (check(TokenKind.VAL) || check(TokenKind.VAR)) {
            return parseDeclare();
        }
        if (check(TokenKind.IDENT) && checkAt(TokenKind.ASSIGN, 1)) {
            return parseAssign();
        }
        return parseExprStmt();
    }

    private Expr parsePrimary() {
        if (matchStep(TokenKind.INT)) {
            Token t = previous();
            return new IntLiteral(t.pos(), Long.parseLong(t.value()));
        }

        if (matchStep(TokenKind.IDENT)) {
            Token t = previous();
            return new Ident(t.pos(), t.value());
        }

        if (matchStep(TokenKind.LPAREN)) {
            Expr e = parseExpression();
            expectStep(TokenKind.RPAREN);
            return e;
        }

        return new ErrorExpr(peek().pos(), "unexpected token '" + peek().kind() + "'");
    }

    private Expr parseUnary() {
        if (matchStep(TokenKind.MINUS)) {
            Expr operand = parseUnary();
            return new Unary(previous().pos(), Op.MINUS, operand);
        }

        return parsePrimary();
    }

    private Expr parseMult() {
        Expr left = parseUnary();

        while (matchStep(TokenKind.MULT, TokenKind.DIV)) {
            Token op = previous();
            Expr right = parseUnary();
            left = new BinOp(previous().pos(), toOp(op), left, right);
        }

        return left;
    }

    private Expr parseAdditive() {
        Expr left = parseMult();

        while (matchStep(TokenKind.PLUS, TokenKind.MINUS)) {
            Token op = previous();
            Expr right = parseMult();
            left = new BinOp(previous().pos(), toOp(op), left, right);
        }

        return left;
    }

    private Expr parseExpression() {
        return parseAdditive();
    }

    private Stmt parseExprStmt() {
        Expr expr = parseExpression();

        expectStep(TokenKind.SEMI);

        return new ExprStmt(previous().pos(), expr);
    }

    private Stmt parseReturn() {
        expectStep(TokenKind.RETURN);
        Expr value = parseExpression();
        expectStep(TokenKind.SEMI);

        return new Return(previous().pos(), value);
    }

    private Stmt parseDeclare() {
        Mut mut;
        if (check(TokenKind.VAL)) {
            mut = Mut.VAL;
        } else if (check(TokenKind.VAR)) {
            mut = Mut.VAR;
        } else {
            throw error("expected VAL or VAR, got " + peek().kind());
        }
        step();

        Token name = expectStep(TokenKind.IDENT);
        Expr expr;

        if (matchStep(TokenKind.ASSIGN)) {
            expr = parseExpression();
        } else {
            if (check(TokenKind.SEMI)) step();
            expr = parseExpression();
        }

        while (!check(TokenKind.SEMI) && !check(TokenKind.EOF)) step();
        if (check(TokenKind.SEMI)) step();

        return new Declare(previous().pos(), mut, name.value(), expr);
    }

    private Stmt parseAssign() {
        Token name = expectStep(TokenKind.IDENT);
        Ident target = new Ident(name.pos(), name.value());

        expectStep(TokenKind.ASSIGN);
        Expr value = parseExpression();
        expectStep(TokenKind.SEMI);

        return new Assign(previous().pos(), target, value);
    }
}