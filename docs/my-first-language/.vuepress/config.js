module.exports = {
  title: 'My First Language',
  description: 'My First Language',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'GitHub', link: 'https://github.com/xiangganLuo/my-language' }
    ],
    sidebar: {
      '/': [
        {
          title: '第一部分 引导与上手',
          collapsible: true,
          children: [
            ['/part-1-intro/01-why-language', '第1章 为什么要自己做一门语言'],
            ['/part-1-intro/02-quickstart', '第2章 5分钟跑通我的第一段程序']
          ]
        },
        {
          title: '第二部分 从字符到执行：编译器主线',
          collapsible: true,
          children: [
            ['/part-2-pipeline/03-lexing', '第3章 词法分析：把字符切成 Token'],
            ['/part-2-pipeline/04-parsing', '第4章 语法分析：把 Token 组装成结构'],
            ['/part-2-pipeline/05-ast', '第5章 AST：抽象语法树的设计与构建'],
            ['/part-2-pipeline/06-semantics', '第6章 语义分析：类型、符号与错误'],
            ['/part-2-pipeline/07-codegen', '第7章 字节码生成：让 AST 变成 JVM 指令'],
            ['/part-2-pipeline/08-runtime', '第8章 运行时：加载与执行']
          ]
        },
        {
          title: '第三部分 工程与调试',
          collapsible: true,
          children: [
            ['/part-3-engineering/09-cli-and-debug', '第9章 CLI 与调试开关的正确打开方式'],
            ['/part-3-engineering/10-testing', '第10章 用测试拆层理解编译流程'],
            ['/part-3-engineering/11-build', '第11章 构建与生成：Maven 管道与插件']
          ]
        },
        {
          title: '第四部分 语言扩展与实践',
          collapsible: true,
          children: [
            ['/part-4-extensions/12-extensions', '第12章 一步步扩展语言能力'],
            ['/part-4-extensions/13-optimizations', '第13章 优化与可调试性'],
            ['/part-4-extensions/14-cases', '第14章 案例与练习']
          ]
        },
        {
          title: '附录',
          collapsible: true,
          children: [
            ['/appendix/glossary', '术语表（初学者视角）'],
            ['/appendix/faq', '常见问题（FAQ）'],
            ['/appendix/cheatsheet', '命令速查与工作流'],
            ['/appendix/references', '参考资料与延伸阅读']
          ]
        }
      ]
    },
    // 显示所有页面的标题链接
    displayAllHeaders: true,
    // 侧边栏深度
    sidebarDepth: 2,
  },
  plugins: [
    'mermaidjs'
  ]
} 