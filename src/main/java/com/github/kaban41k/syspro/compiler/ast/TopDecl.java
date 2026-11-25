package com.github.kaban41k.syspro.compiler.ast;

public sealed interface TopDecl extends Node permits
        ExternDecl,
        FuncDecl {
}