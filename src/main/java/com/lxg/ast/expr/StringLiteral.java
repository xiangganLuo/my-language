package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 字符串字面量，如 "hello"。
 * @author xiangganluo
 */
public class StringLiteral implements Expression {
    public final SourcePos pos;
    public final String value;
    public StringLiteral(String value) { this(null, value); }
    public StringLiteral(SourcePos pos, String value) { this.pos = pos; this.value = value; }
    @Override public SourcePos getPos() { return pos; }
} 