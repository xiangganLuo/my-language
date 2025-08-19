# 命令速查与工作流

- 构建

```bash
mvn -q -DskipTests clean package
```

- 运行与调试

```bash
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg --dump-tokens
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg --dump-parse-tree | cat
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg --dump-ast
java -jar target/my-language-0.1.0-SNAPSHOT.jar examples/hello.lxg --emit-class=out/Program.class
javap -v out/Program.class | sed -n '1,200p'
```

- 测试

```bash
mvn -q test
mvn -q -Dtest=LexerSmokeTest test
mvn -q -Dtest=LxgEndToEndTest test
```

- 推荐工作流
    1) 修改 g4/AST/生成
    2) 逐层开启 dump 验证
    3) 运行单测与示例
    4) 必要时导出 .class 与 `javap -v` 