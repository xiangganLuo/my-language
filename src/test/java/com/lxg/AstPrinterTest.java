package com.lxg;

import com.lxg.antlr.LxgLexer;
import com.lxg.antlr.LxgParser;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.frontend.AstBuilder;
import com.lxg.tools.AstPrinter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * AST 打印器测试：断言关键结构文本存在。
 * <p>
 * 测试意图：
 * - 确认打印器输出包含主要语法结构的可读描述，便于学习/调试
 * <p>
 * 覆盖点：
 * - let 声明与二元表达式的左结合折叠（1 + 2）
 * - if/else 结构与条件表达式存在性
 * - print 语句的节点展示
 *
 * @author xiangganluo
 */
public class AstPrinterTest {
    @Test
    public void print_basic_ast() {
        String src = "let x = 1 + 2; if (x > 1) { print x; } else { print 0; }";
        LxgLexer lexer = new LxgLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LxgParser parser = new LxgParser(tokens);
        CompilationUnit unit = new AstBuilder().build(parser.prog());
        String out = AstPrinter.print(unit);
        assertTrue(out.contains("Let x = (1 ADD 2)".replace("ADD", "ADD"))); // 操作符枚举名称
        assertTrue(out.contains("If cond="));
        assertTrue(out.contains("Print x"));
    }
} 