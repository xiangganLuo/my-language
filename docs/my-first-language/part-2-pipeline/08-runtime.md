# 第8章 运行时：加载与执行

# 一、前言
生成的 `.class` 如何在内存中变成可执行的 `main`？本章用自定义 ClassLoader 与反射完成“定义类 → 获取 main → 调用”。

# 二、目标
- 理解双亲委派与 findClass 的职责
- 掌握反射调用 `main(String[])` 的参数传递细节
- 能在内存中加载并执行生成的类

# 三、设计
术语说明：
- 双亲委派：优先委派父加载器，找不到再由子加载器定义
- 反射：运行时获取方法并调用

核心流程图：
```mermaid
flowchart LR
  Bytes[byte[] class] --> CL[InMemoryClassLoader]
  CL --> K[loadClass]
  K --> R[Reflect main invoke]
```

架构交互图：
```mermaid
graph TD
  ClassGenerator -->|byte[]| InMemoryClassLoader -->|Class<?>| LxgShell -->|invoke| main
```

# 四、实现
目录树（关注项）：
```text
src/main/java/com/lxg/runtime/InMemoryClassLoader.java
src/main/java/com/lxg/runtime/LxgShell.java
```

命令：
```bash
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg --emit-class=out/Program.class
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg
```

代码对照：内存类加载器（仅识别固定类名）
```27:35:src/main/java/com/lxg/runtime/InMemoryClassLoader.java
@Override
protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (name.equals("com.lxg.gen.Program")) {
        return defineClass(name, bytes, 0, bytes.length);
    }
    return super.findClass(name);
}
```

代码对照：反射调用 `main`
```23:34:src/main/java/com/lxg/runtime/LxgShell.java
public void run(byte[] classBytes) {
    try {
        ClassLoader loader = new InMemoryClassLoader(classBytes);
        Class<?> cls = loader.loadClass("com.lxg.gen.Program");
        Method main = cls.getMethod("main", String[].class);
        main.invoke(null, (Object) new String[0]);
    } catch (Throwable t) {
        t.printStackTrace(System.err);
        throw new RuntimeException("Execution failed", t);
    }
}
```

# 五、测试
- 端到端：`LxgEndToEndTest` 即覆盖内存加载与反射路径
- 手动：修改示例后重复运行，观察输出变化

# 六、总结
- 运行时最小实现：单类加载 + 反射 main 调用；若需多类加载，可扩展为 Map<类名, 字节[]> 