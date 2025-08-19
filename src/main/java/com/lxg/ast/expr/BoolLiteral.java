package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 布尔字面量：true 或 false。
 * @author xiangganluo
 */
public class BoolLiteral implements Expression {
    public final SourcePos pos;
    public final boolean value;
    public BoolLiteral(boolean value) { this(null, value); }
    public BoolLiteral(SourcePos pos, boolean value) { this.pos = pos; this.value = value; }
    @Override public SourcePos getPos() { return pos; }
} 