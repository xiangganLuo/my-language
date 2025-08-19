package com.lxg.ast.stmt;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;
import com.lxg.ast.node.Statement;

/**
 * 赋值语句：<name> = <expr>; 将表达式结果存入已声明的局部变量。
 *
 * @author xiangganluo
 */
public class AssignStmt implements Statement {
    public final SourcePos pos;
    public final String name;
    public final Expression value;

    public AssignStmt(String name, Expression value) {
        this(null, name, value);
    }

    public AssignStmt(SourcePos pos, String name, Expression value) {
        this.pos = pos;
        this.name = name;
        this.value = value;
    }

    @Override
    public SourcePos getPos() {
        return pos;
    }
} 