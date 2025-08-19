package com.lxg.sema;

import com.lxg.ast.node.ValueType;

import java.util.*;

/**
 * 符号表（语义分析阶段）：管理变量名到局部变量槽位（index）与类型的映射。
 * main(String[]) 方法中，index=0 保留给参数；用户变量从 1 开始分配。
 *
 * 学习要点：
 * - JVM 的局部变量通过“槽位下标”寻址（int/boolean 占 1 槽，引用类型占 1 槽）
 * - 本实现采用“平坦作用域”：未引入作用域栈，后续可以扩展为 Block 级作用域
 * - 声明（declare）时分配下一个可用槽位；解析（resolve）时按名字查找对应 Local
 *
 * 设计取舍：
 * - 先保证直观易懂，避免一次性引入多层作用域带来的复杂性
 * - 将“槽位分配”与“类型记忆”统一放在 Local 中，便于后续 codegen 使用
 *
 * 使用建议：在 TypeChecker 与 CodeEmitter 中共享同一份符号表，保证类型与槽位一致。
 *
 * author xiangganluo
 */
public class SymbolTable {
    private final Map<String, Local> nameToLocal = new LinkedHashMap<>();
    private int nextIndex = 1; // 0 reserved for String[] args in main

    /** 声明新变量并分配局部槽位。 */
    public Local declare(String name, ValueType type) {
        if (nameToLocal.containsKey(name)) {
            throw new IllegalStateException("Variable already declared: " + name);
        }
        int index = nextIndex;
        nextIndex += 1; // 简化：统一按 1 槽计（本语言仅用到 I* 与 A* 指令）
        Local local = new Local(index, type);
        nameToLocal.put(name, local);
        return local;
    }

    /** 解析变量名，返回对应的局部变量信息；若不存在则抛错。 */
    public Local resolve(String name) {
        Local local = nameToLocal.get(name);
        if (local == null) throw new IllegalStateException("Unknown variable: " + name);
        return local;
    }
} 