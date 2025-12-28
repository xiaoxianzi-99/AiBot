# AI Chat Bot - JavaFX 对话应用

这是一个使用 JavaFX 开发的智能对话应用程序，提供了美观的聊天界面和 AI 对话功能。

## 功能特性

- 🎨 现代化的聊天界面
- 💬 实时对话交互（支持流式响应）
- 🤖 AI 智能回复（DeepSeek API 集成）
- 📝 **Markdown 渲染支持**（支持标题、粗体、列表、代码块等）
- 📎 文件上传分析功能（支持文本文件、代码文件等）
- 📱 响应式设计
- 🎯 支持中英文交互

## 技术栈

- **JavaFX 17**: 用户界面框架
- **Maven**: 项目构建和依赖管理
- **OkHttp**: HTTP 客户端（用于 DeepSeek API 调用）
- **Gson**: JSON 处理库
- **SnakeYAML**: YAML 配置文件解析
- **Flexmark**: Markdown 渲染引擎（支持 GFM 扩展）

## 项目结构

```
AiBot/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── pei/
│       │           ├── AiBotApplication.java      # 主应用程序入口
│       │           ├── controller/
│       │           │   └── ChatController.java    # 聊天控制器
│       │           ├── model/
│       │           │   └── Message.java           # 消息模型
│       │           └── service/
│       │               └── AiService.java         # AI 服务
│       └── resources/
│           ├── fxml/
│           │   └── chat-view.fxml                 # 聊天界面 FXML
│           └── css/
│               └── style.css                      # 样式表
├── pom.xml                                        # Maven 配置
└── README.md                                      # 项目说明
```

## 快速开始

### 环境要求

- Java 17 或更高版本
- Maven 3.6 或更高版本

### 构建项目

```bash
mvn clean install
```

### 运行应用

使用 Maven 运行：

```bash
mvn javafx:run
```

或者使用 IDE（如 IntelliJ IDEA）直接运行 `AiBotApplication` 类。

## 使用说明

1. 启动应用后，会看到一个聊天界面
2. 在底部的输入框中输入消息
3. 点击"发送"按钮或按回车键发送消息
4. AI 将以流式方式逐步显示回复，支持 Markdown 格式化
   - 支持 **粗体** 和 *斜体*
   - 支持标题（# H1, ## H2 等）
   - 支持列表（有序和无序）
   - 支持代码块和行内代码
   - 支持表格和任务列表

### 文件上传功能

1. 点击输入框左侧的"📎 上传文件"按钮
2. 选择要分析的文件（支持 .txt, .java, .py, .js, .json, .xml, .md, .csv 等文本格式）
3. 文件大小限制：1MB 以内
4. AI 将自动分析文件内容并提供详细报告

## AI API 集成

当前版本集成了 DeepSeek Chat Completions API，支持流式响应输出。

### 配置 DeepSeek API

#### 方法一：使用环境变量（推荐，最安全）

设置以下环境变量：

```bash
export DEEPSEEK_API_KEY="your-deepseek-api-key-here"
```

Windows PowerShell:
```powershell
$env:DEEPSEEK_API_KEY="your-deepseek-api-key-here"
```

#### 方法二：修改配置文件

1. 打开 `src/main/resources/application.yml`
2. 在 `deepseek.apiKey` 字段填入你的 API 密钥（不推荐，仅用于测试）

配置示例：
```yaml
deepseek:
  apiUrl: "https://api.deepseek.com/chat/completions"
  apiKey: ""  # 留空，使用环境变量 DEEPSEEK_API_KEY
  model: "deepseek-chat"
```

### 安全注意事项

⚠️ **重要**: 永远不要将 API 密钥硬编码在源代码中或提交到版本控制系统。始终使用环境变量或安全的配置管理系统。

### API 功能

- ✅ 流式响应输出
- ✅ 多轮对话上下文保持
- ✅ Markdown 格式化回复
- ✅ 文件内容分析

## 自定义配置

### 修改样式

编辑 `src/main/resources/css/style.css` 文件可以自定义界面样式，包括：

- 颜色主题
- 字体大小
- 消息气泡样式
- 按钮效果

### 修改界面布局

编辑 `src/main/resources/fxml/chat-view.fxml` 文件可以调整界面布局。

## 开发计划

- [x] 支持多轮对话上下文
- [x] 支持流式响应输出
- [x] 支持 Markdown 渲染
- [x] 支持文件上传和分析
- [ ] 添加对话历史记录
- [ ] 支持语音输入
- [ ] 添加设置界面
- [ ] 支持主题切换
- [ ] 多语言支持

## 贡献

欢迎提交 Issue 和 Pull Request！

## 作者

帕斯卡的芦苇

## 许可证

本项目采用 MIT 许可证。
