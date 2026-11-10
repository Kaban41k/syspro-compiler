package com.github.kaban41k.syspro.compiler.parser;

import com.github.kaban41k.syspro.compiler.ast.Mut;
import com.github.kaban41k.syspro.compiler.ast.Op;
import com.github.kaban41k.syspro.compiler.ast.ProgramNode;
import com.github.kaban41k.syspro.compiler.ast.expr.BinOp;
import com.github.kaban41k.syspro.compiler.ast.expr.Ident;
import com.github.kaban41k.syspro.compiler.ast.expr.IntLiteral;
import com.github.kaban41k.syspro.compiler.ast.expr.Unary;
import com.github.kaban41k.syspro.compiler.ast.stmt.Assign;
import com.github.kaban41k.syspro.compiler.ast.stmt.Declare;
import com.github.kaban41k.syspro.compiler.ast.stmt.ExprStmt;
import com.github.kaban41k.syspro.compiler.ast.stmt.Return;
import com.github.kaban41k.syspro.compiler.common.Position;
import com.github.kaban41k.syspro.compiler.lexer.Lexer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParserTest {

    private static ProgramNode parse(String src) {
        return new Parser(new Lexer(src).tokenize()).parse();
    }

    @Test
    void parseLongMax() {
        String code = ("return " + Long.MAX_VALUE + ";");

        ProgramNode expected = new ProgramNode(
                new Position(1, code.length()),
                List.of(new Return(
                        new Position(1, code.length()),
                        new IntLiteral(new Position(1, 8), Long.MAX_VALUE)
                ))
        );

        assertEquals(expected, parse(code));
    }

    @Test
    void parseIntLiteral() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 10),
                List.of(new Return(
                        new Position(1, 10),
                        new IntLiteral(new Position(1, 8), 42)
                ))
        );
        assertEquals(expected, parse("return 42;"));
    }

    @Test
    void parseIdent() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 9),
                List.of(new Return(
                        new Position(1, 9),
                        new Ident(new Position(1, 8), "x")
                ))
        );
        assertEquals(expected, parse("return x;"));
    }

    @Test
    void parseAdd() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 6),
                List.of(new ExprStmt(
                        new Position(1, 6),
                        new BinOp(
                                new Position(1, 5),
                                Op.PLUS,
                                new IntLiteral(new Position(1, 1), 1),
                                new IntLiteral(new Position(1, 5), 2)
                        )
                ))
        );
        assertEquals(expected, parse("1 + 2;"));
    }

    @Test
    void parsePrecedenceMulBeforeAdd() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 10),
                List.of(new ExprStmt(
                        new Position(1, 10),
                        new BinOp(
                                new Position(1, 9),
                                Op.PLUS,
                                new IntLiteral(new Position(1, 1), 1),
                                new BinOp(
                                        new Position(1, 9),
                                        Op.MULT,
                                        new IntLiteral(new Position(1, 5), 2),
                                        new IntLiteral(new Position(1, 9), 3)
                                )
                        )
                ))
        );
        assertEquals(expected, parse("1 + 2 * 3;"));
    }

    @Test
    void parseLeftAssociativity() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 10),
                List.of(new ExprStmt(
                        new Position(1, 10),
                        new BinOp(
                                new Position(1, 9),
                                Op.MINUS,
                                new BinOp(
                                        new Position(1, 5),
                                        Op.MINUS,
                                        new IntLiteral(new Position(1, 1), 1),
                                        new IntLiteral(new Position(1, 5), 2)
                                ),
                                new IntLiteral(new Position(1, 9), 3)
                        )
                ))
        );
        assertEquals(expected, parse("1 - 2 - 3;"));
    }

    @Test
    void parseParensDoNotCreateNode() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 5),
                List.of(new ExprStmt(
                        new Position(1, 5),
                        new IntLiteral(new Position(1, 2), 42)
                ))
        );
        assertEquals(expected, parse("(42);"));
    }

    @Test
    void parseUnaryMinus() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 3),
                List.of(new ExprStmt(
                        new Position(1, 3),
                        new Unary(
                                new Position(1, 2),
                                Op.MINUS,
                                new IntLiteral(new Position(1, 2), 5)
                        )
                ))
        );
        assertEquals(expected, parse("-5;"));
    }

    @Test
    void parseValDeclare() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 11),
                List.of(new Declare(
                        new Position(1, 11),
                        Mut.VAL,
                        "x",
                        new IntLiteral(new Position(1, 9), 10)
                ))
        );
        assertEquals(expected, parse("val x = 10;"));
    }

    @Test
    void parseVarDeclare() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 11),
                List.of(new Declare(
                        new Position(1, 11),
                        Mut.VAR,
                        "x",
                        new IntLiteral(new Position(1, 9), 10)
                ))
        );
        assertEquals(expected, parse("var x = 10;"));
    }

    @Test
    void parseAssign() {
        ProgramNode expected = new ProgramNode(
                new Position(1, 7),
                List.of(new Assign(
                        new Position(1, 7),
                        new Ident(new Position(1, 1), "x"),
                        new IntLiteral(new Position(1, 5), 10)
                ))
        );
        assertEquals(expected, parse("x = 10;"));
    }

    @Test
    void parseMultipleStatements() {
        ProgramNode expected = new ProgramNode(
                new Position(2, 9),
                List.of(
                        new Declare(
                                new Position(1, 11),
                                Mut.VAR,
                                "x",
                                new IntLiteral(new Position(1, 9), 10)
                        ),
                        new Return(
                                new Position(2, 9),
                                new Ident(new Position(2, 8), "x")
                        )
                )
        );
        assertEquals(expected, parse("var x = 10;\nreturn x;"));
    }
}