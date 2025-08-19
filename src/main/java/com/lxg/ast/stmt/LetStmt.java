package com.lxg.ast.stmt;

import com.lxg.ast.node.Statement;
import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * let <name> = <expr>; 语句。声明一个新的局部变量并使用表达式结果初始化。
 * @author xiangganluo
 */
public class LetStmt implements Statement {
    public final SourcePos pos;
    public final String name;
    public final Expression value;
    public LetStmt(String name, Expression value) { this(null, name, value); }
    public LetStmt(SourcePos pos, String name, Expression value) {
        this.pos = pos;
        this.name = name;
        this.value = value;
    }
    @Override public SourcePos getPos() { return pos; }
} 