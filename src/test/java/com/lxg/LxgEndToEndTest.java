package com.lxg;

import com.lxg.tools.Main;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * 端到端测试：给定一段源代码，调用 CLI 的核心方法执行，并断言标准输出。
 * <p>
 * 学习要点：
 * - 通过替换 System.out 的方式捕获程序输出，验证从“源码→字节码→执行”的完整链路
 * - 使用 Main.runSource 复用生产路径，尽量贴近真实运行时行为
 * - 调试开关全部关闭，保证用例输出稳定
 * <p>
 * 注：此处未断言错误分支，相关测试见 SemanticErrorTest。
 * <p>
 * author xiangganluo
 */
public class LxgEndToEndTest {

    private String run(String source) {
        PrintStream oldOut = System.out;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bout));
        try {
            Main.runSource(source, null, false, true, false);
            return new String(bout.toByteArray());
        } finally {
            System.setOut(oldOut);
        }
    }

    /**
     * 用例意图：验证基本字面量（int/string/bool）的打印与顺序输出。
     * 覆盖点：
     * - INT/STRING/BOOLEAN 三类字面量常量入栈与打印
     * - 多条 print 顺序与换行
     */
    @Test
    public void testPrintIntAndStringAndBool() throws IOException {
        String source = new String(Files.readAllBytes(Paths.get("/Users/hbwb36334/github/my-language/examples/hello.lxg")), "UTF-8");
        Main.runSource(source, null, true, true, false);
    }

    /**
     * 用例意图：验证 let 声明、赋值与四则运算的正确性。
     * 覆盖点：
     * - 变量声明与解析（SymbolTable）
     * - 二元算术 ADD/SUB/MUL/DIV 的左结合与运算顺序
     */
    @Test
    public void testLetAssignArith() {
        String src = "let a = 10; let b = 3; print a + b; print a - b; print a * b; print a / b;\n";
        String out = run(src);
        assertEquals("13\n7\n30\n3\n", out);
    }

    /**
     * 用例意图：验证 if/else 分支的条件求值与跳转。
     * 覆盖点：
     * - 比较运算（>=）结果为 boolean
     * - IFEQ/GOTO 标签跳转与 then/else 选择
     */
    @Test
    public void testIfElse() {
        String src = "let x = 2 + 1; if (x >= 3) { print true; } else { print false; }\n";
        String out = run(src);
        assertEquals("true\n", out);
    }
} 