package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 二元表达式：<left> <op> <right>。
 * @author xiangganluo
 */
public class BinaryExpr implements Expression {
    public final SourcePos pos;
    public final Expression left;
    public final BinaryOp op;
    public final Expression right;
    public BinaryExpr(Expression left, BinaryOp op, Expression right) { this(null, left, op, right); }
    public BinaryExpr(SourcePos pos, Expression left, BinaryOp op, Expression right) {
        this.pos = pos;
        this.left = left;
        this.op = op;
        this.right = right;
    }
    @Override public SourcePos getPos() { return pos; }
} 