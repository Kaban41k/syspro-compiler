package com.github.kaban41k.syspro.compiler.parser;

import com.github.kaban41k.syspro.compiler.ast.ProgramNode;
import com.github.kaban41k.syspro.compiler.ast.expr.IntLiteral;
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
    void parsesReturnIntLiteral() {
        String code = ("return " + Long.MAX_VALUE +";");

        ProgramNode expected = new ProgramNode(
                new Position(1, code.length()),
                List.of(new Return(
                        new Position(1, code.length()),
                        new IntLiteral(new Position(1, 8), Long.MAX_VALUE)
                ))
        );

        assertEquals(expected, parse(code));
    }
}