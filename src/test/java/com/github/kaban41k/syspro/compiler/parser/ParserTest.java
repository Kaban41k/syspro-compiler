package com.github.kaban41k.syspro.compiler.parser;

import com.github.kaban41k.syspro.compiler.ast.FuncDecl;
import com.github.kaban41k.syspro.compiler.ast.Mut;
import com.github.kaban41k.syspro.compiler.ast.Op;
import com.github.kaban41k.syspro.compiler.ast.ProgramNode;
import com.github.kaban41k.syspro.compiler.ast.expr.BinOp;
import com.github.kaban41k.syspro.compiler.ast.expr.Ident;
import com.github.kaban41k.syspro.compiler.ast.expr.IntLiteral;
import com.github.kaban41k.syspro.compiler.ast.expr.Unary;
import com.github.kaban41k.syspro.compiler.ast.stmt.Assign;
import com.github.kaban41k.syspro.compiler.ast.stmt.Block;
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

    private static ProgramNode parseBody(String body) {
        return parse("def main() {\n" + body + "}\n");
    }

    @Test
    void parseLongMax() {
        String num = Long.toString(Long.MAX_VALUE);
        int semiCol = "    return ".length() + num.length() + 1;

        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Return(
                                        new Position(2, semiCol),
                                        new IntLiteral(new Position(2, 12), Long.MAX_VALUE)
                                ))
                        )
                ))
        );

        assertEquals(expected, parseBody("    return " + num + ";\n"));
    }

    @Test
    void parseIntLiteral() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Return(
                                        new Position(2, 14),
                                        new IntLiteral(new Position(2, 12), 42)
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    return 42;\n"));
    }

    @Test
    void parseIdent() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Return(
                                        new Position(2, 13),
                                        new Ident(new Position(2, 12), "x")
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    return x;\n"));
    }

    @Test
    void parseAdd() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new ExprStmt(
                                        new Position(2, 10),
                                        new BinOp(
                                                new Position(2, 9),
                                                Op.PLUS,
                                                new IntLiteral(new Position(2, 5), 1),
                                                new IntLiteral(new Position(2, 9), 2)
                                        )
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    1 + 2;\n"));
    }

    @Test
    void parsePrecedenceMulBeforeAdd() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new ExprStmt(
                                        new Position(2, 14),
                                        new BinOp(
                                                new Position(2, 13),
                                                Op.PLUS,
                                                new IntLiteral(new Position(2, 5), 1),
                                                new BinOp(
                                                        new Position(2, 13),
                                                        Op.MULT,
                                                        new IntLiteral(new Position(2, 9), 2),
                                                        new IntLiteral(new Position(2, 13), 3)
                                                )
                                        )
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    1 + 2 * 3;\n"));
    }

    @Test
    void parseLeftAssociativity() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new ExprStmt(
                                        new Position(2, 14),
                                        new BinOp(
                                                new Position(2, 13),
                                                Op.MINUS,
                                                new BinOp(
                                                        new Position(2, 9),
                                                        Op.MINUS,
                                                        new IntLiteral(new Position(2, 5), 1),
                                                        new IntLiteral(new Position(2, 9), 2)
                                                ),
                                                new IntLiteral(new Position(2, 13), 3)
                                        )
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    1 - 2 - 3;\n"));
    }

    @Test
    void parseParensDoNotCreateNode() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new ExprStmt(
                                        new Position(2, 9),
                                        new IntLiteral(new Position(2, 6), 42)
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    (42);\n"));
    }

    @Test
    void parseUnaryMinus() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new ExprStmt(
                                        new Position(2, 7),
                                        new Unary(
                                                new Position(2, 6),
                                                Op.MINUS,
                                                new IntLiteral(new Position(2, 6), 5)
                                        )
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    -5;\n"));
    }

    @Test
    void parseValDeclare() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Declare(
                                        new Position(2, 15),
                                        Mut.VAL,
                                        "x",
                                        new IntLiteral(new Position(2, 13), 10)
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    val x = 10;\n"));
    }

    @Test
    void parseVarDeclare() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Declare(
                                        new Position(2, 15),
                                        Mut.VAR,
                                        "x",
                                        new IntLiteral(new Position(2, 13), 10)
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    var x = 10;\n"));
    }

    @Test
    void parseAssign() {
        ProgramNode expected = new ProgramNode(
                new Position(3, 1),
                List.of(new FuncDecl(
                        new Position(3, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(3, 1),
                                List.of(new Assign(
                                        new Position(2, 11),
                                        new Ident(new Position(2, 5), "x"),
                                        new IntLiteral(new Position(2, 9), 10)
                                ))
                        )
                ))
        );
        assertEquals(expected, parseBody("    x = 10;\n"));
    }

    @Test
    void parseMultipleStatements() {
        ProgramNode expected = new ProgramNode(
                new Position(4, 1),
                List.of(new FuncDecl(
                        new Position(4, 1),
                        "main",
                        List.of(),
                        new Block(
                                new Position(4, 1),
                                List.of(
                                        new Declare(
                                                new Position(2, 15),
                                                Mut.VAR,
                                                "x",
                                                new IntLiteral(new Position(2, 13), 10)
                                        ),
                                        new Return(
                                                new Position(3, 13),
                                                new Ident(new Position(3, 12), "x")
                                        )
                                )
                        )
                ))
        );
        assertEquals(expected, parseBody("    var x = 10;\n    return x;\n"));
    }
}