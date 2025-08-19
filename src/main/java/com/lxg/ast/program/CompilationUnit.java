package com.lxg.ast.program;

import com.lxg.ast.node.Node;
import com.lxg.ast.node.SourcePos;
import com.lxg.ast.node.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 程序根节点，表示整个源文件/程序。
 *
 * @author xiangganluo
 */
public class CompilationUnit implements Node {
    public final SourcePos pos;
    /**
     * 程序中按顺序出现的顶层语句列表。
     */
    public final List<Statement> statements;

    public CompilationUnit(List<Statement> statements) {
        this(null, statements);
    }

    public CompilationUnit(SourcePos pos, List<Statement> statements) {
        this.pos = pos;
        this.statements = Collections.unmodifiableList(new ArrayList<>(statements));
    }

    @Override
    public SourcePos getPos() {
        return pos;
    }
} 