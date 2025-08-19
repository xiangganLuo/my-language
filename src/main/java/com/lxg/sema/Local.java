package com.lxg.sema;

import com.lxg.ast.node.ValueType;

/**
 * 局部变量描述：包含槽位索引与静态类型。
 * @author xiangganluo
 */
public class Local {
    public final int index;
    public final ValueType type;
    public Local(int index, ValueType type) { this.index = index; this.type = type; }
} 