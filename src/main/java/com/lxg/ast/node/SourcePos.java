package com.lxg.ast.node;

/**
 * 源码位置（行、列）。行/列均从 1/0 开始（列与 ANTLR 的 charPositionInLine 一致）。
 *
 * @author xiangganluo
 */
public final class SourcePos {
    public final int line;
    public final int column;

    public SourcePos(int line, int column) {
        this.line = line;
        this.column = column;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }
} 