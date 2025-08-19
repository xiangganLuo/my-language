package com.lxg.runtime;

/**
 * 内存类加载器：将生成的字节码数组定义为 JVM 中的类。
 * 仅识别固定类名 "com.lxg.gen.Program"，其余委托父加载器。
 * <p>
 * 学习要点：
 * - ClassLoader 委派模型：优先委派给父加载器，找不到再由子加载器定义
 * - defineClass：将字节数组转换为 JVM 内部的 Class 对象
 * - 该实现仅演示“单类加载”，满足当前教学目的
 * <p>
 * 使用建议：
 * - 如需加载多个类，可将 bytes 改为 Map<String, byte[]>，按 name 返回对应字节
 * - 也可重写 loadClass 细化委派与缓存策略
 *
 * @author xiangganluo
 */
class InMemoryClassLoader extends ClassLoader {
    private final byte[] bytes; // 单类字节码：固定映射到 com.lxg.gen.Program

    InMemoryClassLoader(byte[] bytes) {
        // 指定父加载器为当前类的加载器，遵循标准委派模型
        super(InMemoryClassLoader.class.getClassLoader());
        this.bytes = bytes;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // 仅当请求的名称是我们生成的固定类名时，才使用内存字节码进行定义
        if (name.equals("com.lxg.gen.Program")) {
            return defineClass(name, bytes, 0, bytes.length);
        }
        // 其他名称全部交给父加载器处理（保持隔离与安全）
        return super.findClass(name);
    }
} 