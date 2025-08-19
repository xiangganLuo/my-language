package com.lxg.sema;

import com.lxg.ast.node.Expression;
import com.lxg.ast.node.Node;
import com.lxg.ast.node.SourcePos;
import com.lxg.ast.node.Statement;
import com.lxg.ast.node.ValueType;
import com.lxg.ast.program.CompilationUnit;
import com.lxg.ast.stmt.*;
import com.lxg.ast.expr.*;

/**
 * 最小语义检查（教学版）：
 * - 声明与使用：变量须先 let 声明后再使用/赋值
 * - 类型检查：算术运算只接受 INT，比较运算只接受 INT，! 只接受 BOOLEAN
 * - if 条件类型必须为 BOOLEAN
 *
 * 简化假设：
 * - 使用单一符号表，未构建作用域栈（Block 遮蔽等高级特性可在后续版本引入）
 * - 诊断信息携带 SourcePos（行:列），便于快速定位错误来源
 *
 * 使用建议：在 codegen 之前运行，若存在错误则打印后终止后续阶段
 *
 * @author xiangganluo
 */
public class TypeChecker {
    public Diagnostics check(CompilationUnit unit) {
        Diagnostics diags = new Diagnostics();
        SymbolTable symbols = new SymbolTable();
        for (Statement s : unit.statements) {
            checkStatement(s, symbols, diags);
        }
        return diags;
    }

    /**
     * 构造位置后缀：用于拼接到错误信息后，形如 " at line:col"
     */
    private static String at(Node n) {
        SourcePos p = n.getPos();
        return p == null ? "" : (" at " + p.toString());
    }

    /**
     * 语句级检查：
     * - let：推断右值类型并声明到符号表
     * - 赋值：解析左侧变量，检查右值类型可赋性
     * - print：仅验证右值可推断
     * - if：条件为 BOOLEAN，再递归检查 then/else 块
     */
    private void checkStatement(Statement s, SymbolTable symbols, Diagnostics diags) {
        if (s instanceof LetStmt) {
            LetStmt ls = (LetStmt) s;
            ValueType t = infer(ls.value, symbols, diags);
            try {
                symbols.declare(ls.name, t);
            } catch (IllegalStateException e) {
                diags.error(e.getMessage() + at(ls));
            }
        } else if (s instanceof AssignStmt) {
            AssignStmt as = (AssignStmt) s;
            Local local;
            try {
                local = symbols.resolve(as.name);
            } catch (IllegalStateException e) {
                diags.error(e.getMessage() + at(as));
                return;
            }
            ValueType rt = infer(as.value, symbols, diags);
            if (local.type != rt) {
                diags.error("Type mismatch for variable '" + as.name + "': expected " + local.type + ", got " + rt + at(as));
            }
        } else if (s instanceof PrintStmt) {
            infer(((PrintStmt) s).expression, symbols, diags);
        } else if (s instanceof BlockStmt) {
            // 简化实现：未引入作用域栈，直接在同一符号表中检查
            for (Statement child : ((BlockStmt) s).statements) {
                checkStatement(child, symbols, diags);
            }
        } else if (s instanceof IfStmt) {
            ValueType ct = infer(((IfStmt) s).condition, symbols, diags);
            if (ct != ValueType.BOOLEAN) {
                diags.error("if condition must be boolean" + at((IfStmt) s));
            }
            BlockStmt thenB = ((IfStmt) s).thenBlock;
            for (Statement child : thenB.statements) checkStatement(child, symbols, diags);
            BlockStmt elseB = ((IfStmt) s).elseBlock;
            if (elseB != null) for (Statement child : elseB.statements) checkStatement(child, symbols, diags);
        } else {
            diags.error("Unknown statement: " + s.getClass().getSimpleName() + at(s));
        }
    }

    /**
     * 表达式类型推断：递归地对字面量、变量引用、一元/二元表达式进行检查与类型返回。
     * 错误不会抛异常，而是落到 Diagnostics 以便一次性展示所有问题。
     */
    private ValueType infer(Expression e, SymbolTable symbols, Diagnostics diags) {
        if (e instanceof IntLiteral) return ValueType.INT;
        if (e instanceof StringLiteral) return ValueType.STRING;
        if (e instanceof BoolLiteral) return ValueType.BOOLEAN;
        if (e instanceof VarRef) {
            try {
                return symbols.resolve(((VarRef) e).name).type;
            } catch (IllegalStateException ex) {
                diags.error(ex.getMessage() + at((VarRef) e));
                return ValueType.VOID;
            }
        }
        if (e instanceof UnaryExpr) {
            UnaryExpr ue = (UnaryExpr) e;
            ValueType t = infer(ue.expr, symbols, diags);
            switch (ue.op) {
                case PLUS:
                case MINUS:
                    if (t != ValueType.INT) diags.error("Unary +/- expects INT" + at(ue));
                    return ValueType.INT;
                case NOT:
                    if (t != ValueType.BOOLEAN) diags.error("Unary ! expects BOOLEAN" + at(ue));
                    return ValueType.BOOLEAN;
            }
        }
        if (e instanceof BinaryExpr) {
            BinaryExpr be = (BinaryExpr) e;
            ValueType lt = infer(be.left, symbols, diags);
            ValueType rt = infer(be.right, symbols, diags);
            switch (be.op) {
                case ADD: case SUB: case MUL: case DIV:
                    if (lt != ValueType.INT || rt != ValueType.INT) diags.error("Arithmetic expects INT operands" + at(be));
                    return ValueType.INT;
                case EQ: case NE: case LT: case GT: case LE: case GE:
                    if (lt != ValueType.INT || rt != ValueType.INT) diags.error("Comparison expects INT operands" + at(be));
                    return ValueType.BOOLEAN;
            }
        }
        diags.error("Unknown expression: " + e.getClass().getSimpleName() + at(e));
        return ValueType.VOID;
    }
} 