package com.lxg.runtime;

import java.lang.reflect.Method;

/**
 * 运行时 Shell：通过自定义类加载器加载生成的类，并反射调用 main 方法执行。
 *
 * 学习要点：
 * - InMemoryClassLoader 只识别固定名称 com.lxg.gen.Program，将传入的字节数组 define 为此类
 * - 反射调用静态方法 main(String[])，等价于正常入口执行
 * - 与真实 ClassLoader 隔离，避免污染当前进程的类命名空间
 *
 * 使用建议：
 * - 如果未来生成多个类，可将 InMemoryClassLoader 改为 Map<类名, 字节[]> 的多类加载方案
 * - 捕获的异常栈会打印到标准错误，便于快速定位运行时异常
 *
 * @author xiangganluo
 */
public class LxgShell {
    /**
     * 执行传入的类字节码：定义类 -> 反射获取 main(String[]) -> 调用。
     */
    public void run(byte[] classBytes) {
        try {
            ClassLoader loader = new InMemoryClassLoader(classBytes);
            Class<?> cls = loader.loadClass("com.lxg.gen.Program");
            Method main = cls.getMethod("main", String[].class);
            // 反射调用静态方法时，参数需以 (Object) String[] 形式传入
            main.invoke(null, (Object) new String[0]);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
            throw new RuntimeException("Execution failed", t);
        }
    }
} 