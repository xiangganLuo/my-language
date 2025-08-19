package com.lxg.tools;

import com.lxg.antlr.LxgLexer;
import com.lxg.antlr.LxgParser;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.codegen.ClassGenerator;
import com.lxg.frontend.AstBuilder;
import com.lxg.runtime.LxgShell;
import com.lxg.sema.Diagnostics;
import com.lxg.sema.TypeChecker;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 命令行入口：解析 -> 构建 AST -> 语义检查 -> 生成字节码 -> 运行。
 * 支持调试开关：
 * - --dump-tokens：打印词法 Token，用于观察“字符→Token”阶段
 * - --dump-parse-tree：打印解析树，用于观察“Token→语法结构”阶段
 * - --dump-ast：打印 AST，用于观察“解析树→AST 抽象化”结果
 * - --emit-class=<path>：写入 .class，便于 javap -v 反汇编观察指令序列
 *
 * 使用建议：先层层 dump（tokens/parse-tree/ast）再 emit-class，帮助建立从源码到字节码的心智模型。
 *
 * @author xiangganluo
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Usage: java -jar my-language.jar <source.lxg> [--emit-class=out/Program.class] [--dump-tokens] [--dump-parse-tree] [--dump-ast]");
            System.out.println("Example source:\n  let x = 1 + 2;\n  if (x > 2) { print x; } else { print 0; }\n  print 42;");
            return;
        }

        String sourcePath = null;
        String emitClassPath = null;
        boolean dumpTokens = false;
        boolean dumpParseTree = false;
        boolean dumpAst = false;

        // 解析命令行参数：识别调试开关与源文件
        for (String arg : args) {
            if (arg.startsWith("--emit-class=")) {
                emitClassPath = arg.substring("--emit-class=".length());
            } else if ("--dump-tokens".equals(arg)) {
                dumpTokens = true;
            } else if ("--dump-parse-tree".equals(arg)) {
                dumpParseTree = true;
            } else if ("--dump-ast".equals(arg)) {
                dumpAst = true;
            } else if (!arg.startsWith("--")) {
                sourcePath = arg;
            }
        }
        if (sourcePath == null) {
            throw new IllegalArgumentException("Missing source file path");
        }

        String source = new String(Files.readAllBytes(Paths.get(sourcePath)), "UTF-8");
        runSource(source, emitClassPath, dumpTokens, dumpParseTree, dumpAst);
    }

    /**
     * 执行一次完整的编译与运行流程：
     * 1) 词法/语法分析：由 ANTLR 生成的 Lexer/Parser 完成，必要时 dump tokens/parse tree
     * 2) AST 构建：使用 AstBuilder 将 Parse Tree 转为简洁 AST，必要时 dump AST
     * 3) 语义检查：TypeChecker 进行声明/类型检查，错误集中输出并终止
     * 4) 代码生成：ClassGenerator/CodeEmitter 生成 Program.main 的字节码
     * 5) 运行：LxgShell 内存加载字节码并反射调用 main
     */
    public static void runSource(String source, String emitClassPath, boolean dumpTokens, boolean dumpParseTree, boolean dumpAst) {
        try {
            // 1) Lexer/Parser
            LxgLexer lexer = new LxgLexer(CharStreams.fromString(source));
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            if (dumpTokens) {
                tokens.fill();
                for (Token t : tokens.getTokens()) {
                    System.out.println(t.getText() + " -> " + LxgLexer.VOCABULARY.getDisplayName(t.getType()));
                }
            }
            LxgParser parser = new LxgParser(tokens);
            LxgParser.ProgContext prog = parser.prog();
            if (dumpParseTree) {
                System.out.println(prog.toStringTree(parser));
            }

            // 2) AST
            AstBuilder builder = new AstBuilder();
            CompilationUnit unit = builder.build(prog);
            if (dumpAst) {
                System.out.println(AstPrinter.print(unit));
            }

            // 3) Semantic check
            Diagnostics diags = new TypeChecker().check(unit);
            if (diags.hasErrors()) {
                diags.printAll(System.err);
                return; // 中止
            }

            // 4) Codegen
            ClassGenerator gen = new ClassGenerator();
            byte[] cls = gen.generate(unit);

            // 5) 可选：写出 .class，便于 javap -v 调试
            if (emitClassPath != null && !emitClassPath.isEmpty()) {
                Path out = Paths.get(emitClassPath);
                Files.createDirectories(out.getParent() == null ? Paths.get(".") : out.getParent());
                Files.write(out, cls);
                System.out.println("Emitted class to: " + out.toAbsolutePath());
            }

            // 6) 在内存中加载并执行 main
            new LxgShell().run(cls);
        } catch (Exception e) {
            e.printStackTrace(System.err);
            throw new RuntimeException("Failed to run source", e);
        }
    }
} 