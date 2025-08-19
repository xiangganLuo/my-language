package com.lxg;

import com.lxg.antlr.LxgLexer;
import com.lxg.antlr.LxgParser;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.frontend.AstBuilder;
import com.lxg.sema.Diagnostics;
import com.lxg.sema.TypeChecker;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * 语义错误测试：覆盖未声明变量与类型不匹配两类基础错误。
 * <p>
 * 测试意图：
 * - 验证 TypeChecker 能发现并报告典型语义错误
 * <p>
 * 覆盖点：
 * - 使用未声明变量应产生错误，错误消息包含关键字串（Unknown variable）
 * - 赋值类型不匹配（int <- string）应产生错误，错误消息包含关键字串（Type mismatch）
 * - 本组用例不校验行列位置文本，仅验证错误分类与关键信息
 *
 * @author xiangganluo
 */
public class SemanticErrorTest {
    @Test
    public void undeclared_variable() {
        String src = "print x;";
        LxgLexer lexer = new LxgLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LxgParser parser = new LxgParser(tokens);
        CompilationUnit unit = new AstBuilder().build(parser.prog());
        Diagnostics diags = new TypeChecker().check(unit);
        assertTrue(diags.hasErrors());
        assertTrue(diags.getErrors().stream().anyMatch(e -> e.contains("Unknown variable")));
    }

    @Test
    public void type_mismatch_assignment() {
        String src = "let x = 1; x = \"hello\";";
        LxgLexer lexer = new LxgLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LxgParser parser = new LxgParser(tokens);
        CompilationUnit unit = new AstBuilder().build(parser.prog());
        Diagnostics diags = new TypeChecker().check(unit);
        assertTrue(diags.hasErrors());
        assertTrue(diags.getErrors().stream().anyMatch(e -> e.contains("Type mismatch")));
    }
} 