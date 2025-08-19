package com.lxg.codegen;

import com.lxg.ast.node.Statement;
import com.lxg.ast.program.CompilationUnit;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.*;

/**
 * 类生成器：为整个程序生成一个包含 public static void main(String[]) 的类。
 * 使用 ClassWriter(COMPUTE_FRAMES|COMPUTE_MAXS) 自动计算栈帧与最大栈深度，简化栈管理。
 * 生成类名固定为 com.lxg.gen.Program，运行时通过内存类加载器加载该类并调用 main。
 *
 * @author xiangganluo
 */
public class ClassGenerator {

    private static final String CLASS_NAME = "com/lxg/gen/Program";

    /**
     * 根据 AST 生成字节码。返回的字节数组可直接写入 .class 或由类加载器加载。
     */
    public byte[] generate(CompilationUnit unit) {
        // 创建类：public class com.lxg.gen.Program extends java.lang.Object
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(V1_8, ACC_PUBLIC | ACC_SUPER, CLASS_NAME, null, "java/lang/Object", null);

        // 生成默认构造方法：public Program(){ super(); }
        MethodVisitor ctor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        ctor.visitCode();
        ctor.visitVarInsn(ALOAD, 0);
        ctor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        ctor.visitInsn(RETURN);
        ctor.visitMaxs(0, 0); // 由 COMPUTE_* 自动计算
        ctor.visitEnd();

        // 生成入口：public static void main(String[] args)
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        // 发射主体语句
        com.lxg.sema.SymbolTable symbols = new com.lxg.sema.SymbolTable();
        CodeEmitter emitter = new CodeEmitter(mv, symbols);
        for (Statement s : unit.statements) {
            emitter.emitStatement(s);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0); // 由 COMPUTE_* 自动计算
        mv.visitEnd();

        cw.visitEnd();
        return cw.toByteArray();
    }
} 