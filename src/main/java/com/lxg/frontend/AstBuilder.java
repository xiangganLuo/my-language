package com.lxg.frontend;

import com.lxg.antlr.*;
import com.lxg.ast.node.*;
import com.lxg.ast.expr.*;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.ast.stmt.*;

import java.util.*;

/**
 * 前端构建器：将 ANTLR 解析树（Parse Tree）转换为自定义 AST 结构。
 * 学习要点：
 * 1) Parse Tree 保留了语法细节（如括号、分隔符），AST 仅保留与语义相关的节点
 * 2) 本类以 Visitor 模式访问各层规则节点，按优先级自低向高（equality → comparison → ... → primary）构建 AST
 * 3) 二元运算按“左结合”构造：使用 (op right)* 的迭代方式，依次折叠为 BinaryExpr
 * 4) 每个 AST 节点都会携带 SourcePos，便于后续语义诊断定位
 *
 * 提示：若不熟悉 ANTLR 生成的 Context API，可用 --dump-parse-tree 观察树形结构，再对照本类的 visit 方法
 *
 * @author xiangganluo
 */
public class AstBuilder extends LxgBaseVisitor<Object> {

    private static SourcePos pos(org.antlr.v4.runtime.Token t) {
        if (t == null) return null;
        return new SourcePos(t.getLine(), t.getCharPositionInLine());
    }

    /**
     * 从顶层解析规则构建 AST 根节点：CompilationUnit 只包含按顺序排列的 Statement 列表。
     */
    public CompilationUnit build(LxgParser.ProgContext ctx) {
        List<Statement> statements = new ArrayList<>();
        for (LxgParser.StmtContext sc : ctx.stmt()) {
            statements.add((Statement) visit(sc));
        }
        return new CompilationUnit(pos(ctx.getStart()), statements);
    }

    /**
     * 语句分发：根据第一个词决定语句种类（print/let/if），或回退为 block/赋值。
     */
    @Override
    public Object visitStmt(LxgParser.StmtContext ctx) {
        if (ctx.getChild(0).getText().equals("print")) {
            Expression expr = (Expression) visit(ctx.expr());
            return new PrintStmt(pos(ctx.getStart()), expr);
        }
        if (ctx.getChild(0).getText().equals("let")) {
            String name = ctx.ID().getText();
            Expression expr = (Expression) visit(ctx.expr());
            return new LetStmt(pos(ctx.getStart()), name, expr);
        }
        // 赋值：ID '=' expr ';'
        if (ctx.ID() != null && ctx.expr() != null && ctx.getChildCount() >= 4 && "=".equals(ctx.getChild(1).getText())) {
            String name = ctx.ID().getText();
            Expression expr = (Expression) visit(ctx.expr());
            return new AssignStmt(pos(ctx.getStart()), name, expr);
        }
        // if/else
        if (ctx.getChild(0).getText().equals("if")) {
            Expression cond = (Expression) visit(ctx.expr());
            BlockStmt thenBlk = (BlockStmt) visit(ctx.block(0));
            BlockStmt elseBlk = ctx.block().size() > 1 ? (BlockStmt) visit(ctx.block(1)) : null;
            return new IfStmt(pos(ctx.getStart()), cond, thenBlk, elseBlk);
        }
        // 纯 block
        if (!ctx.block().isEmpty()) {
            return visit(ctx.block(0));
        }
        throw new IllegalStateException("Unknown statement: " + ctx.getText());
    }

    /**
     * 语句块：顺序收集子语句，位置取 '{' 的 token 位置。
     */
    @Override
    public Object visitBlock(LxgParser.BlockContext ctx) {
        List<Statement> statements = new ArrayList<>();
        for (LxgParser.StmtContext sc : ctx.stmt()) {
            statements.add((Statement) visit(sc));
        }
        return new BlockStmt(pos(ctx.getStart()), statements);
    }

    /**
     * 顶层 expr 直接转发给 equality（最低优先级的二元运算层）。
     */
    @Override
    public Object visitExpr(LxgParser.ExprContext ctx) {
        return visit(ctx.equality());
    }

    /**
     * equality：处理 ==/!=，以左结合方式连成链。
     * 示例：a == b != c 解析为 ((a == b) != c)
     */
    @Override
    public Object visitEquality(LxgParser.EqualityContext ctx) {
        Object left = visit(ctx.comparison(0));
        for (int i = 1; i < ctx.comparison().size(); i++) {
            String opText = ctx.getChild(2 * i - 1).getText();
            Object right = visit(ctx.comparison(i));
            left = new BinaryExpr(pos(ctx.getStart()), (Expression) left,
                    "==".equals(opText) ? BinaryOp.EQ : BinaryOp.NE,
                    (Expression) right);
        }
        return left;
    }

    /**
     * comparison：处理 < > <= >=，同样按左结合构造。
     */
    @Override
    public Object visitComparison(LxgParser.ComparisonContext ctx) {
        Object left = visit(ctx.addition(0));
        for (int i = 1; i < ctx.addition().size(); i++) {
            String opText = ctx.getChild(2 * i - 1).getText();
            Object right = visit(ctx.addition(i));
            BinaryOp op;
            switch (opText) {
                case "<": op = BinaryOp.LT; break;
                case ">": op = BinaryOp.GT; break;
                case "<=": op = BinaryOp.LE; break;
                case ">=": op = BinaryOp.GE; break;
                default: throw new IllegalStateException("Unknown op: " + opText);
            }
            left = new BinaryExpr(pos(ctx.getStart()), (Expression) left, op, (Expression) right);
        }
        return left;
    }

    /**
     * addition：处理 + -，按左结合折叠为 BinaryExpr。
     */
    @Override
    public Object visitAddition(LxgParser.AdditionContext ctx) {
        Object left = visit(ctx.multiplication(0));
        for (int i = 1; i < ctx.multiplication().size(); i++) {
            String opText = ctx.getChild(2 * i - 1).getText();
            Object right = visit(ctx.multiplication(i));
            BinaryOp op = "+".equals(opText) ? BinaryOp.ADD : BinaryOp.SUB;
            left = new BinaryExpr(pos(ctx.getStart()), (Expression) left, op, (Expression) right);
        }
        return left;
    }

    /**
     * multiplication：处理 * /，按左结合折叠。
     */
    @Override
    public Object visitMultiplication(LxgParser.MultiplicationContext ctx) {
        Object left = visit(ctx.unary(0));
        for (int i = 1; i < ctx.unary().size(); i++) {
            String opText = ctx.getChild(2 * i - 1).getText();
            Object right = visit(ctx.unary(i));
            BinaryOp op = "*".equals(opText) ? BinaryOp.MUL : BinaryOp.DIV;
            left = new BinaryExpr(pos(ctx.getStart()), (Expression) left, op, (Expression) right);
        }
        return left;
    }

    /**
     * unary：处理一元 + - !，递归到 primary；支持多次前缀（如 --5）。
     */
    @Override
    public Object visitUnary(LxgParser.UnaryContext ctx) {
        if (ctx.unary() != null) {
            String opText = ctx.getChild(0).getText();
            UnaryOp op;
            switch (opText) {
                case "+": op = UnaryOp.PLUS; break;
                case "-": op = UnaryOp.MINUS; break;
                case "!": op = UnaryOp.NOT; break;
                default: throw new IllegalStateException("Unknown unary op: " + opText);
            }
            return new UnaryExpr(pos(ctx.getStart()), op, (Expression) visit(ctx.unary()));
        }
        return visit(ctx.primary());
    }

    /**
     * primary：字面量/变量/括号表达式。
     * 字符串字面量会进行反转义处理（支持 \n \r \t \" \\ 等）。
     */
    @Override
    public Object visitPrimary(LxgParser.PrimaryContext ctx) {
        if (ctx.INT() != null) {
            return new IntLiteral(pos(ctx.getStart()), Integer.parseInt(ctx.INT().getText()));
        }
        if (ctx.STRING() != null) {
            String raw = ctx.STRING().getText();
            String unquoted = unescapeString(raw.substring(1, raw.length() - 1));
            return new StringLiteral(pos(ctx.getStart()), unquoted);
        }
        if (ctx.TRUE() != null) return new BoolLiteral(pos(ctx.getStart()), true);
        if (ctx.FALSE() != null) return new BoolLiteral(pos(ctx.getStart()), false);
        if (ctx.ID() != null) return new VarRef(pos(ctx.getStart()), ctx.ID().getText());
        if (ctx.expr() != null) return visit(ctx.expr());
        throw new IllegalStateException("Unknown primary: " + ctx.getText());
    }

    /**
     * 反转义字符串字面量：将转义序列替换为实际字符。
     * 支持：\n 换行、\r 回车、\t 制表、\" 双引号、\\ 反斜杠。
     */
    private static String unescapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(++i);
                switch (n) {
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    default: sb.append(n); break;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
} 