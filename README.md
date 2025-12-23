# AI Chat Bot - JavaFX 对话应用

这是一个使用 JavaFX 开发的智能对话应用程序，提供了美观的聊天界面和 AI 对话功能。

## 功能特性

- 🎨 现代化的聊天界面
- 💬 实时对话交互
- 🤖 AI 智能回复（支持模拟模式和真实 API 集成）
- 📱 响应式设计
- 🎯 支持中英文交互

## 技术栈

- **JavaFX 17**: 用户界面框架
- **Maven**: 项目构建和依赖管理
- **OkHttp**: HTTP 客户端（用于 AI API 调用）
- **Gson**: JSON 处理库

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
4. AI 将自动回复您的消息

## AI API 集成

当前版本使用模拟响应进行演示。要集成真实的 AI API（如 OpenAI、Claude 等）：

1. 打开 `src/main/java/com/pei/service/AiService.java`
2. 配置 API URL 和 API Key：
   ```java
   private static final String API_URL = "your-api-endpoint";
   private static final String API_KEY = "your-api-key";
   ```
3. 修改 `sendMessage()` 方法，将 `getMockResponse()` 替换为 `sendToRealApi()`

### 支持的 AI API

- OpenAI GPT
- Claude
- 其他兼容 OpenAI API 格式的服务

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

- [ ] 支持多轮对话上下文
- [ ] 添加对话历史记录
- [ ] 支持语音输入
- [ ] 支持文件上传
- [ ] 添加设置界面
- [ ] 支持主题切换
- [ ] 多语言支持

## 贡献

欢迎提交 Issue 和 Pull Request！

## 作者

帕斯卡的芦苇

## 许可证

本项目采用 MIT 许可证。
