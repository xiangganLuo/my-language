package com.lxg.tools;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.Statement;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.ast.expr.*;
import com.lxg.ast.stmt.*;

/**
 * 简易 AST 打印器：将 AST 以缩进文本形式输出，便于学习与调试。
 * @author xiangganluo
 */
public final class AstPrinter {
    private AstPrinter() {}

    public static String print(CompilationUnit unit) {
        StringBuilder sb = new StringBuilder();
        for (Statement s : unit.statements) {
            printStmt(sb, s, 0);
        }
        return sb.toString();
    }

    private static void indent(StringBuilder sb, int n) {
        for (int i = 0; i < n; i++) sb.append(' ');
    }

    private static void printStmt(StringBuilder sb, Statement s, int ind) {
        if (s instanceof LetStmt) {
            LetStmt ls = (LetStmt) s;
            indent(sb, ind); sb.append("Let ").append(ls.name).append(" = ");
            printExpr(sb, ls.value, 0); sb.append('\n');
        } else if (s instanceof AssignStmt) {
            AssignStmt as = (AssignStmt) s;
            indent(sb, ind); sb.append("Assign ").append(as.name).append(" = ");
            printExpr(sb, as.value, 0); sb.append('\n');
        } else if (s instanceof PrintStmt) {
            indent(sb, ind); sb.append("Print ");
            printExpr(sb, ((PrintStmt) s).expression, 0); sb.append('\n');
        } else if (s instanceof BlockStmt) {
            indent(sb, ind); sb.append("Block\n");
            for (Statement c : ((BlockStmt) s).statements) printStmt(sb, c, ind + 2);
        } else if (s instanceof IfStmt) {
            IfStmt is = (IfStmt) s;
            indent(sb, ind); sb.append("If cond=");
            printExpr(sb, is.condition, 0); sb.append("\n");
            indent(sb, ind); sb.append("Then:\n");
            for (Statement c : is.thenBlock.statements) printStmt(sb, c, ind + 2);
            if (is.elseBlock != null) {
                indent(sb, ind); sb.append("Else:\n");
                for (Statement c : is.elseBlock.statements) printStmt(sb, c, ind + 2);
            }
        } else {
            indent(sb, ind); sb.append("UnknownStmt ").append(s.getClass().getSimpleName()).append('\n');
        }
    }

    private static void printExpr(StringBuilder sb, Expression e, int ind) {
        if (e instanceof IntLiteral) sb.append(((IntLiteral) e).value);
        else if (e instanceof StringLiteral) sb.append('"').append(((StringLiteral) e).value).append('"');
        else if (e instanceof BoolLiteral) sb.append(((BoolLiteral) e).value);
        else if (e instanceof VarRef) sb.append(((VarRef) e).name);
        else if (e instanceof UnaryExpr) {
            UnaryExpr ue = (UnaryExpr) e;
            sb.append('(').append(ue.op).append(' ');
            printExpr(sb, ue.expr, 0); sb.append(')');
        } else if (e instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) e;
            sb.append('(');
            printExpr(sb, be.left, 0);
            sb.append(' ').append(be.op).append(' ');
            printExpr(sb, be.right, 0);
            sb.append(')');
        } else {
            sb.append("<UnknownExpr>");
        }
    }
} 