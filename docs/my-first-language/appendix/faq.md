# 常见问题（FAQ）

- Maven 提示命令未找到？
  - 安装：macOS `brew install maven`；或到 Apache Maven 官网下载并配置 PATH
- 运行时报包名重复？
  - 检查 `Lxg.g4` 头部是否含 `@header` 包名；应依赖 `pom.xml` 中 `antlr4-maven-plugin` 的 `packageName`
- `javap` 不可用？
  - 确认 JDK 已安装并在 PATH 中，或使用完整路径 `$(/usr/libexec/java_home)/bin/javap`
- 如何定位编译阶段问题？
  - 按顺序使用 `--dump-tokens`、`--dump-parse-tree`、`--dump-ast`，逐层缩小范围 