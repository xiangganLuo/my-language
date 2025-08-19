package com.lxg.ast.stmt;

import com.lxg.ast.node.Statement;
import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * print <expr>; 语句。将表达式的求值结果输出到标准输出。
 * @author xiangganluo
 */
public class PrintStmt implements Statement {
    public final SourcePos pos;
    public final Expression expression;
    public PrintStmt(Expression expression) { this(null, expression); }
    public PrintStmt(SourcePos pos, Expression expression) { this.pos = pos; this.expression = expression; }
    @Override public SourcePos getPos() { return pos; }
} 