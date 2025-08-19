package com.lxg.codegen;

import com.lxg.ast.node.*;
import com.lxg.ast.expr.*;
import com.lxg.ast.stmt.*;
import com.lxg.sema.Local;
import com.lxg.sema.SymbolTable;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 代码发射器：将 AST 节点翻译为 ASM 字节码指令（基于 JVM 栈机器）。
 * 该类不负责类/方法的创建，只关注语句与表达式的具体指令序列。
 * @author xiangganluo
 */
class CodeEmitter {
    private final MethodVisitor mv;
    private final SymbolTable symbols;

    CodeEmitter(MethodVisitor mv, SymbolTable symbols) {
        this.mv = mv;
        this.symbols = symbols;
    }

    /**
     * 按语句类型分发到相应的生成方法。
     */
    void emitStatement(Statement stmt) {
        if (stmt instanceof PrintStmt) {
            emitPrint((PrintStmt) stmt);
        } else if (stmt instanceof LetStmt) {
            emitLet((LetStmt) stmt);
        } else if (stmt instanceof AssignStmt) {
            emitAssign((AssignStmt) stmt);
        } else if (stmt instanceof BlockStmt) {
            for (Statement s : ((BlockStmt) stmt).statements) emitStatement(s);
        } else if (stmt instanceof IfStmt) {
            emitIf((IfStmt) stmt);
        } else {
            throw new IllegalStateException("Unknown statement: " + stmt.getClass());
        }
    }

    /**
     * 生成 if/else 分支：
     * - 先将条件表达式压栈（0/1）
     * - IFEQ 跳到 else 分支（0 为假）
     * - 执行 then 分支并跳过 else
     * - 贴上 else 与 end 标签，控制流程汇合
     */
    private void emitIf(IfStmt ifs) {
        ValueType condType = emitExpression(ifs.condition);
        if (condType != ValueType.BOOLEAN) {
            throw new IllegalStateException("if condition must be boolean");
        }
        Label elseLabel = new Label();
        Label endLabel = new Label();
        mv.visitJumpInsn(IFEQ, elseLabel); // 栈顶为0则跳到else
        for (Statement s : ifs.thenBlock.statements) emitStatement(s);
        mv.visitJumpInsn(GOTO, endLabel);
        mv.visitLabel(elseLabel);
        if (ifs.elseBlock != null) {
            for (Statement s : ifs.elseBlock.statements) emitStatement(s);
        }
        mv.visitLabel(endLabel);
    }

    /**
     * System.out.println 重载选择：根据值类型调用(I)V/(Ljava/lang/String;)V/(Z)V。
     * 前置条件：表达式已将值压栈。
     */
    private void emitPrint(PrintStmt ps) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        ValueType type = emitExpression(ps.expression);
        if (type == ValueType.INT) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(I)V", false);
        } else if (type == ValueType.STRING) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        } else if (type == ValueType.BOOLEAN) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Z)V", false);
        } else {
            throw new IllegalStateException("Unsupported type in print: " + type);
        }
    }

    /**
     * let 声明：右值先求值压栈 -> 分配局部槽位 -> 存储。
     */
    private void emitLet(LetStmt ls) {
        ValueType type = emitExpression(ls.value);
        Local local = symbols.declare(ls.name, type);
        storeLocal(local);
    }

    /**
     * 赋值：右值先求值压栈 -> 类型校验 -> 存储到已存在槽位。
     */
    private void emitAssign(AssignStmt as) {
        Local local = symbols.resolve(as.name);
        ValueType type = emitExpression(as.value);
        if (local.type != type) {
            throw new IllegalStateException("Type mismatch for variable '" + as.name + "': expected " + local.type + ", got " + type);
        }
        storeLocal(local);
    }

    /**
     * 按类型将栈顶存入本地变量槽位（int/boolean 使用 I* 指令，引用使用 A* 指令）。
     */
    private void storeLocal(Local local) {
        switch (local.type) {
            case INT:
            case BOOLEAN:
                mv.visitVarInsn(ISTORE, local.index);
                break;
            case STRING:
                mv.visitVarInsn(ASTORE, local.index);
                break;
            default:
                throw new IllegalStateException("Unsupported local type: " + local.type);
        }
    }

    /**
     * 将本地变量槽位装载到栈顶，对应 ILOAD/ALOAD。
     */
    private void loadLocal(Local local) {
        switch (local.type) {
            case INT:
            case BOOLEAN:
                mv.visitVarInsn(ILOAD, local.index);
                break;
            case STRING:
                mv.visitVarInsn(ALOAD, local.index);
                break;
            default:
                throw new IllegalStateException("Unsupported local type: " + local.type);
        }
    }

    /**
     * 表达式求值统一入口：将结果压栈并返回静态类型。
     * - 字面量：直接常量入栈
     * - 变量：槽位装载
     * - 复合表达式：递归生成左右子树，再发射操作指令
     */
    ValueType emitExpression(Expression e) {
        if (e instanceof IntLiteral) {
            int v = ((IntLiteral) e).value;
            pushInt(v);
            return ValueType.INT;
        } else if (e instanceof StringLiteral) {
            mv.visitLdcInsn(((StringLiteral) e).value);
            return ValueType.STRING;
        } else if (e instanceof BoolLiteral) {
            mv.visitLdcInsn(((BoolLiteral) e).value ? 1 : 0);
            return ValueType.BOOLEAN;
        } else if (e instanceof VarRef) {
            Local local = symbols.resolve(((VarRef) e).name);
            loadLocal(local);
            return local.type;
        } else if (e instanceof UnaryExpr) {
            return emitUnary((UnaryExpr) e);
        } else if (e instanceof BinaryExpr) {
            return emitBinary((BinaryExpr) e);
        }
        throw new IllegalStateException("Unknown expression: " + e.getClass());
    }

    /**
     * 一元运算：
     * - +x：类型需为 int，指令无操作
     * - -x：类型为 int，发射 INEG
     * - !x：类型为 boolean，借助条件跳转将 0/1 取反
     */
    private ValueType emitUnary(UnaryExpr ue) {
        ValueType t = emitExpression(ue.expr);
        switch (ue.op) {
            case PLUS:
                if (t != ValueType.INT) throw new IllegalStateException("Unary + expects INT");
                return ValueType.INT;
            case MINUS:
                if (t != ValueType.INT) throw new IllegalStateException("Unary - expects INT");
                mv.visitInsn(INEG);
                return ValueType.INT;
            case NOT:
                if (t != ValueType.BOOLEAN) throw new IllegalStateException("Unary ! expects BOOLEAN");
                Label trueL = new Label();
                Label endL = new Label();
                // 栈顶为0(假) -> 跳到trueL 压1，否则压0
                mv.visitJumpInsn(IFEQ, trueL);
                pushInt(0);
                mv.visitJumpInsn(GOTO, endL);
                mv.visitLabel(trueL);
                pushInt(1);
                mv.visitLabel(endL);
                return ValueType.BOOLEAN;
            default:
                throw new IllegalStateException("Unknown unary op: " + ue.op);
        }
    }

    /**
     * 二元运算：
     * - 算术：先左后右压栈 -> 发射 IADD/ISUB/IMUL/IDIV
     * - 比较：先左后右压栈 -> 条件跳转到 true 标签 -> 归一化为 0/1
     */
    private ValueType emitBinary(BinaryExpr be) {
        switch (be.op) {
            case ADD:
            case SUB:
            case MUL:
            case DIV: {
                ValueType lt = emitExpression(be.left);
                ValueType rt = emitExpression(be.right);
                if (lt != ValueType.INT || rt != ValueType.INT) {
                    throw new IllegalStateException("Arithmetic expects INT operands");
                }
                switch (be.op) {
                    case ADD: mv.visitInsn(IADD); break;
                    case SUB: mv.visitInsn(ISUB); break;
                    case MUL: mv.visitInsn(IMUL); break;
                    case DIV: mv.visitInsn(IDIV); break;
                }
                return ValueType.INT;
            }
            case EQ:
            case NE:
            case LT:
            case GT:
            case LE:
            case GE: {
                ValueType lt = emitExpression(be.left);
                ValueType rt = emitExpression(be.right);
                if (lt != ValueType.INT || rt != ValueType.INT) {
                    throw new IllegalStateException("Comparison expects INT operands");
                }
                Label trueL = new Label();
                Label endL = new Label();
                switch (be.op) {
                    case EQ: mv.visitJumpInsn(IF_ICMPEQ, trueL); break;
                    case NE: mv.visitJumpInsn(IF_ICMPNE, trueL); break;
                    case LT: mv.visitJumpInsn(IF_ICMPLT, trueL); break;
                    case GT: mv.visitJumpInsn(IF_ICMPGT, trueL); break;
                    case LE: mv.visitJumpInsn(IF_ICMPLE, trueL); break;
                    case GE: mv.visitJumpInsn(IF_ICMPGE, trueL); break;
                }
                // 未跳转到 trueL 则为假，压0
                pushInt(0);
                mv.visitJumpInsn(GOTO, endL);
                mv.visitLabel(trueL);
                pushInt(1);
                mv.visitLabel(endL);
                return ValueType.BOOLEAN;
            }
            default:
                throw new IllegalStateException("Unsupported binary op: " + be.op);
        }
    }

    /**
     * 常量压栈：根据取值范围选择 ICONST/BIPUSH/SIPUSH/LDC 最短指令形式。
     */
    private void pushInt(int v) {
        if (v >= -1 && v <= 5) {
            switch (v) {
                case -1: mv.visitInsn(ICONST_M1); break;
                case 0: mv.visitInsn(ICONST_0); break;
                case 1: mv.visitInsn(ICONST_1); break;
                case 2: mv.visitInsn(ICONST_2); break;
                case 3: mv.visitInsn(ICONST_3); break;
                case 4: mv.visitInsn(ICONST_4); break;
                case 5: mv.visitInsn(ICONST_5); break;
            }
        } else if (v >= Byte.MIN_VALUE && v <= Byte.MAX_VALUE) {
            mv.visitIntInsn(BIPUSH, v);
        } else if (v >= Short.MIN_VALUE && v <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, v);
        } else {
            mv.visitLdcInsn(v);
        }
    }
} 