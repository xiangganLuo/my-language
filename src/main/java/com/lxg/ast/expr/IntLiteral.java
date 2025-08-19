package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 整数字面量，如 1、42。
 *
 * @author xiangganluo
 */
public class IntLiteral implements Expression {
    public final SourcePos pos;
    public final int value;

    public IntLiteral(int value) {
        this(null, value);
    }

    public IntLiteral(SourcePos pos, int value) {
        this.pos = pos;
        this.value = value;
    }

    @Override
    public SourcePos getPos() {
        return pos;
    }
} 