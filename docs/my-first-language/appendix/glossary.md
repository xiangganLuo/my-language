# 📘 专业术语速览（Glossary）

- 词法分析（Lexical Analysis / Lexer）：将源码字符流切分为最小有意义的“记号”（Token），如标识符、数字、关键字、运算符等。
- 语法分析（Parsing / Parser）：根据文法规则将 Token 序列规约为解析树（Parse Tree），检查是否符合语法结构。
- 解析树（Parse Tree）：严格反映文法推导过程的树，包含所有中间节点，结构较“冗长”。
- 抽象语法树（AST）：对解析树的抽象与简化，仅保留与语义相关的结构，适合后续语义分析与代码生成。
- 语义分析（Semantic Analysis）：在 AST 上进行声明/使用检查、类型检查、作用域与符号表处理，产出诊断信息（Diagnostics）。
- 符号表（Symbol Table）：记录标识符（变量/函数）与其绑定信息（类型、槽位索引、作用域）以支持解析与检查。
- 作用域（Scope）：名字可见性的边界，决定标识符解析的规则。常见有块级、函数级、文件级等。
- 字节码（Bytecode）：JVM 的中间指令集（.class 文件内容），与具体平台无关，由解释器/即时编译器执行。
- 栈帧（Stack Frame）：方法调用时在 JVM 栈上分配的执行上下文，包含局部变量表、操作数栈等。
- 局部变量表（Local Variable Table）：方法内部用于存放参数与局部变量的槽位（按索引寻址）。
- 指令集（Instruction Set）：JVM 支持的操作集合，如 ILOAD/ISTORE/IADD/IFEQ 等。
- 方法描述符（Method Descriptor）：JVM 对方法签名的编码，如 (I)Z 表示入参 int、返回 boolean。
- 类加载器（ClassLoader）：将字节码装载为 JVM 可执行类对象；可自定义来源（内存/网络/文件）。
- 反射（Reflection）：运行时查询类型信息、调用方法或访问字段的能力。
- 代码生成（Codegen）：将 AST 转换为目标平台代码（本项目为 JVM 字节码），需处理类型、指令与控制流。
- 诊断（Diagnostics）：编译阶段的错误/警告信息集合，便于一次性展示给用户。
