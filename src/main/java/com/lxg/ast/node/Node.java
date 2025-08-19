package com.lxg.ast.node;

/**
 * LXG 语言中所有 AST 节点的标记接口。
 * 节点为不可变对象，用于描述源代码的语法结构。
 * @author xiangganluo
 */
public interface Node {
    default SourcePos getPos() { return null; }
} 