package com.lxg.ast.expr;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 变量引用，按名称读取已声明的局部变量的当前值。
 * @author xiangganluo
 */
public class VarRef implements Expression {
    public final SourcePos pos;
    public final String name;
    public VarRef(String name) { this(null, name); }
    public VarRef(SourcePos pos, String name) { this.pos = pos; this.name = name; }
    @Override public SourcePos getPos() { return pos; }
} 