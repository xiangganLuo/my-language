package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 一元表达式：op expr。
 *
 * @author xiangganluo
 */
public class UnaryExpr implements Expression {
    public final SourcePos pos;
    public final UnaryOp op;
    public final Expression expr;

    public UnaryExpr(UnaryOp op, Expression expr) {
        this(null, op, expr);
    }

    public UnaryExpr(SourcePos pos, UnaryOp op, Expression expr) {
        this.pos = pos;
        this.op = op;
        this.expr = expr;
    }

    @Override
    public SourcePos getPos() {
        return pos;
    }
} 