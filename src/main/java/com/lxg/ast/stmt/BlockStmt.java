package com.lxg.ast.stmt;

import com.lxg.ast.node.SourcePos;
import com.lxg.ast.node.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 语句块：以 { ... } 包裹的多条语句顺序执行。
 *
 * @author xiangganluo
 */
public class BlockStmt implements Statement {
    public final SourcePos pos;
    public final List<Statement> statements;

    public BlockStmt(List<Statement> statements) {
        this(null, statements);
    }

    public BlockStmt(SourcePos pos, List<Statement> statements) {
        this.pos = pos;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }

    @Override
    public SourcePos getPos() {
        return pos;
    }
} 