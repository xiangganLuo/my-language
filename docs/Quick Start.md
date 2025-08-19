# 学习路径（Learning Path）

本学习路径从易到难，帮助你循序渐进掌握“从源码到字节码执行”的完整链路。所有示例均以 Java 8 与 Maven 为前提，建议边读边改边跑，用 `javap -v` 对照验证理解。

## 第一阶段：环境准备与跑通主线

- 安装依赖：
  - JDK 1.8（Java 8）
  - Maven（macOS 可使用 `brew install maven`）
- 构建项目：
  - `mvn -q -DskipTests clean package`
- 运行一个最小示例：
  1) 创建文件 `examples/hello.lxg`
  ```
  print "hello";
  ```
  2) 启动 CLI（打包后的可执行 fat-jar）：
  ```
  java -jar target/language-lxg-*-jar-with-dependencies.jar examples/hello.lxg
  ```
- 观察“源码→执行”的主干流程：Lexer/Parser → AST → 语义检查 → 字节码 → 内存加载执行

## 第二阶段：透视调试开关（建议依次开启）

- `--dump-tokens`：观察词法阶段输出（字符→Token）。
  ```
  java -jar target/language-lxg-*-jar-with-dependencies.jar examples/hello.lxg --dump-tokens
  ```
- `--dump-parse-tree`：观察语法树（Token→语法结构）。
  ```
  java -jar target/language-lxg-*-jar-with-dependencies.jar examples/hello.lxg --dump-parse-tree
  ```
- `--dump-ast`：观察 AST（解析树→抽象语法树）。
  ```
  java -jar target/language-lxg-*-jar-with-dependencies.jar examples/hello.lxg --dump-ast
  ```
- `--emit-class=<path>`：生成 `.class` 以便 `javap -v` 反汇编学习。
  ```
  java -jar target/language-lxg-*-jar-with-dependencies.jar examples/hello.lxg --emit-class=out/Program.class
  javap -v out/Program.class | sed -n '1,200p'
  ```

使用建议：先按顺序 dump（tokens → parse-tree → ast），再观察字节码，有助于建立层层抽象的心智模型。

## 第三阶段：按模块深入（代码入口与关键类）

- antlr（`src/main/antlr4/com/lxg/antlr/Lxg.g4`）
  - 词法/语法规则、优先级分层：`expr → equality → comparison → addition → multiplication → unary → primary`
  - 关键 Token：`INT`、`STRING`、`TRUE`、`FALSE`、`ID`，以及保留字和操作符
- frontend（`com.lxg.frontend.AstBuilder`）
  - Parse Tree → AST 的转换，二元表达式“左结合”折叠，节点 `SourcePos` 采集
- ast
  - 分包结构：`ast/node`（`Node`、`Expression`、`Statement`、`ValueType`、`SourcePos`）、`ast/expr`、`ast/stmt`、`ast/program`
  - 设计原则：不可变、类型明确、语义导向
- sema（`com.lxg.sema`）
  - `SymbolTable`：变量名→局部槽位与类型映射（平坦作用域，后续可扩展块级作用域）
  - `TypeChecker`：最小声明与类型检查；`Diagnostics`：收集错误，消息含 `SourcePos`（行:列）
- codegen（`com.lxg.codegen`）
  - `ClassGenerator` 生成 `com.lxg.gen.Program.main`，`CodeEmitter` 发射语句/表达式的字节码
  - 关注点：JVM 栈机器、指令选择、`Label` 跳转、`ClassWriter.COMPUTE_FRAMES | COMPUTE_MAXS`
- runtime（`com.lxg.runtime`）
  - `InMemoryClassLoader` 内存加载单类；`LxgShell` 反射调用 `main(String[])`
- tools（`com.lxg.tools.Main`）
  - CLI 管道：解析 → AST → 语义 → 生成 → 运行；调试开关统一入口

## 第四阶段：示例与语义错误定位

- 运行基础示例：
  - `examples/arithmetic.lxg`、`examples/conditions.lxg`
- 观察语义错误：
  - 未声明变量：`examples/error_undeclared.lxg`
  - 类型不匹配：`examples/error_type_mismatch.lxg`
  - 执行方式：
    ```
    java -jar target/language-lxg-*-jar-with-dependencies.jar examples/error_undeclared.lxg
    java -jar target/language-lxg-*-jar-with-dependencies.jar examples/error_type_mismatch.lxg
    ```
  - 诊断输出包含源位置，如 `[ERROR] Unknown variable: x at line:col`

## 第五阶段：用测试来拆层理解

- 运行全部测试：`mvn -q test`
- 针对性运行：
  - 词法冒烟：`mvn -q -Dtest=LexerSmokeTest test`
  - AST 打印：`mvn -q -Dtest=AstPrinterTest test`
  - 语义错误：`mvn -q -Dtest=SemanticErrorTest test`
  - 端到端：`mvn -q -Dtest=LxgEndToEndTest test`
- 覆盖关系（与代码阶段对应）：
  - LexerSmokeTest → 词法切分是否符合预期
  - AstPrinterTest → AST 结构化表达是否可读
  - SemanticErrorTest → TypeChecker 能否发现典型错误（未声明、类型不匹配）
  - LxgEndToEndTest → 从源码到执行的整链路产出是否符合预期（打印、算术、分支）

## 第六阶段：动手扩展（建议按难度递增）

- 新运算符：取模 `%`
  - 修改 `Lxg.g4`（grammar 与优先级）、AST（新增/复用 `BinaryOp`/`BinaryExpr`）、codegen（`IREM` 指令）
- 新语句：`while (cond) { ... }`
  - 语法新增 → AST 新节点 → codegen 用 `Label` + 条件跳转
- 作用域：块级遮蔽
  - `SymbolTable` 引入作用域栈；`TypeChecker` 与 `CodeEmitter` 同步变更
- 函数：
  - 语法 `fun name(params){...}` 与 `return`；多方法生成与 `INVOKESTATIC`
- 调试增强：
  - `LineNumberTable`、`SourceFile` 输出，提升 `javap -v` 可读性

## 第七阶段：工程化与优化

- 测试拆层与用例设计：覆盖正常路径与错误路径，构造边界数据
- 错误信息工程化：按阶段分类输出，附定位与修复建议
- 性能与可读性：
  - 常量折叠、死代码消除、短路求值
  - 保持代码清晰（命名、函数粒度、注释“讲为什么不讲怎么做”）

## 附录：常用命令速查

- 构建：`mvn -q -DskipTests clean package`
- 运行：`java -jar target/language-lxg-*-jar-with-dependencies.jar <file.lxg> [--dump-tokens] [--dump-parse-tree] [--dump-ast] [--emit-class=out/Program.class]`
- 反汇编：`javap -v out/Program.class | sed -n '1,200p'`
- 单测：`mvn -q -Dtest=ClassName test` 