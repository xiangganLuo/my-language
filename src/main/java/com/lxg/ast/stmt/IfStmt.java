package com.lxg.ast.stmt;

import com.lxg.ast.node.Statement;
import com.lxg.ast.node.Expression;
import com.lxg.ast.node.SourcePos;

/**
 * 条件分支语句：if (condition) thenBlock else elseBlock。
 * elseBlock 可能为 null。
 * @author xiangganluo
 */
public class IfStmt implements Statement {
    public final SourcePos pos;
    public final Expression condition;
    public final BlockStmt thenBlock;
    public final BlockStmt elseBlock; // may be null
    public IfStmt(Expression condition, BlockStmt thenBlock, BlockStmt elseBlock) { this(null, condition, thenBlock, elseBlock); }
    public IfStmt(SourcePos pos, Expression condition, BlockStmt thenBlock, BlockStmt elseBlock) {
        this.pos = pos;
        this.condition = condition;
        this.thenBlock = thenBlock;
        this.elseBlock = elseBlock;
    }
    @Override public SourcePos getPos() { return pos; }
} 