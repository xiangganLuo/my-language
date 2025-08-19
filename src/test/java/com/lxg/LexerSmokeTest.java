package com.lxg;

import com.lxg.antlr.LxgLexer;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * 词法层冒烟测试：简单断言 token 序列包含关键类型。
 * <p>
 * 测试意图：
 * - 验证基础源码能被切分为期望的 Token 序列
 * <p>
 * 覆盖点：
 * - 关键字与标识符（ID）的区分（'let'/'print' 不应被当作 ID）
 * - 整数字面量（INT）被正确识别
 * - 空白与分号不影响 Token 序列的连续性
 *
 * @author xiangganluo
 */
public class LexerSmokeTest {
    @Test
    public void tokens_basic() {
        String src = "let x = 1 + 2; print x;";
        LxgLexer lexer = new LxgLexer(CharStreams.fromString(src));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        List<String> kinds = new ArrayList<>();
        for (Token t : tokens.getTokens()) {
            kinds.add(LxgLexer.VOCABULARY.getDisplayName(t.getType()));
        }
        assertTrue(kinds.contains("ID"));
        assertTrue(kinds.contains("INT"));
        assertTrue(kinds.stream().anyMatch(k -> k.contains("'let'")));
        assertTrue(kinds.stream().anyMatch(k -> k.contains("'print'")));
    }
} 