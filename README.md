# AiBot：基于 JavaFX 的本地 AI 对话应用

AiBot 是一个基于 JavaFX 的桌面端 AI 对话应用，使用 DeepSeek API 进行大模型对话，支持**流式回答展示**、**SQLite 本地对话记录**、**左侧会话列表管理**和**简单 Markdown 渲染**，界面以中文为主，适合本地日常使用和二次开发。

---

## 1. 项目特性与主要功能

### 1.1 主要功能

- ✅ **AI 对话（DeepSeek API）**
  - 支持与大模型多轮对话
  - 支持**流式输出**，回答逐字出现，体验接近在线聊天工具

- ✅ **本地对话记录（SQLite）**
  - 使用 `aibot.db` 本地数据库保存会话和消息
  - 左侧会话列表可快速切换历史会话
  - 可持久化保存对话内容，方便回顾

- ✅ **简易 Markdown 显示**
  - 支持常见 Markdown 语法的基础渲染
  - 适合展示代码片段、列表、标题等内容

- ✅ **中文友好界面**
  - 主界面文案为中文
  - 针对本地开发环境与网络环境做了简单适配提示

### 1.2 技术栈

- **Java 17+**（可根据你的项目版本调整）
- **JavaFX**（桌面 UI 框架）
- **DeepSeek API**（大模型对话）
- **SQLite**（本地数据库）
- 构建工具：**Maven**（`pom.xml`）

---

## 2. 运行环境与前置要求

### 2.1 Java 环境

- 推荐使用：
  - **JDK 17 或更高版本**
- 检查版本（Windows PowerShell）：

```powershell
java -version
```

### 2.2 JavaFX 依赖说明

本项目采用 Maven 管理依赖，在 `pom.xml` 中已经添加 JavaFX 相关依赖和运行配置：

- 如果你使用的是 **IntelliJ IDEA**：
  - 直接导入 Maven 项目，IDE 通常会自动处理 JavaFX 依赖
  - 运行配置选择 `Main` 或 `AiBotApplication` 即可

- 如果你本机没有单独安装 JavaFX SDK：
  - 一般不需要手动安装，Maven 会自动下载 JavaFX 依赖
  - 遇到 JavaFX 相关错误，可参考下文「常见问题」章节

### 2.3 DeepSeek API 访问

- 需要准备：
  - 一个可用的 **DeepSeek API Key**
  - 稳定的网络环境，能访问 DeepSeek API 服务

### 2.4 数据库

- 项目根目录自带 `aibot.db` 文件（SQLite 数据库）
  - 可直接使用该文件
  - 也可以删除后，程序在首次运行时重新创建（视实现而定，如有差异请按项目真实逻辑调整说明）

---

## 3. 快速开始

### 3.1 克隆项目

在 PowerShell 中执行：

```powershell
cd D:\Project\JavaFX
git clone <你的仓库地址> AiBot
cd AiBot
```

> 如果你已经在本地有该项目，可跳过此步。

### 3.2 配置 `application.yml`

配置文件位置：`src/main/resources/application.yml`

根据你的实际情况，填入 DeepSeek API 配置信息，例如：

- API Key
- 模型名称
- 接口地址（如果有自建代理 / 网关）

> 具体字段说明见下文「DeepSeek API 配置说明」。

### 3.3 使用 IDE 运行（推荐）

1. 使用 **IntelliJ IDEA** 打开项目根目录（包含 `pom.xml` 的目录）
2. 等待 Maven 自动下载依赖
3. 找到启动类（例如）：
   - `com.pei.Main`
   - 或 `com.pei.AiBotApplication`
4. 右键运行 `Run 'Main'` / `Run 'AiBotApplication'`
5. 程序启动后，会打开 JavaFX 界面，左侧为会话列表，右侧为对话区域

### 3.4 使用 Maven 命令行运行（可选）

在项目根目录执行：

```powershell
mvn clean package

# 运行已打包好的 JAR（示例）
java -jar target\AiBot-1.0-SNAPSHOT.jar
```

> 如运行 JAR 报 JavaFX 相关错误，请优先用 IDE 方式运行，或根据 `pom.xml` 中的 JavaFX 插件说明进行调整。

---

## 4. DeepSeek API 配置说明

配置文件：`src/main/resources/application.yml`  
服务类参考：`com.pei.service.AiService`、`com.pei.service.Config`

### 4.1 常见配置字段（示例）

根据你在 `application.yml` 中的定义，一般会包括：

- `deepseek.apiKey`：你的 DeepSeek API Key
- `deepseek.baseUrl`：API 基础地址（如使用官方或代理）
- `deepseek.model`：默认模型名称（例如 `deepseek-chat` 等）
- `deepseek.timeout`：请求超时时间（可选）

你可以在 `README` 中示意：

- 不要硬编码真实 Key
- 建议使用环境变量（如在 IDE 运行配置中设置）或本机仅用的 dev 配置文件

### 4.2 API Key 安全小贴士

- **不要** 将真实的 API Key 提交到 Git 仓库
- 建议：
  - 使用 `.gitignore` 忽略本地的敏感配置
  - 或在 `application.yml` 中引用环境变量（视你的实现而定）

---

## 5. SQLite 数据库说明

数据库文件：`aibot.db`（位于项目根目录）

### 5.1 用途

- 存储会话和消息记录：
  - `Conversation` 表：保存每个会话的基本信息
  - `Message` 表：保存每条对话消息（角色、内容、时间等）
- 由 `com.pei.service.DatabaseService` 负责读写

### 5.2 备份与迁移

- 如需备份对话记录：
  - 直接拷贝 `aibot.db` 文件至其他位置即可
- 如需迁移至另一台机器：
  - 携带 `aibot.db` 一起拷贝
  - 放在同样的项目根目录下（或根据你的配置调整路径说明）

### 5.3 重置数据

- 如果你想清空所有历史会话：
  - 备份或删除 `aibot.db` 文件
  - 下次启动程序时，将根据代码逻辑重新创建新的数据库（如果项目中有自动建表逻辑）

---

## 6. 使用说明（简要）

### 6.1 创建新会话

- 打开应用后：
  - 左侧可通过按钮 / 操作创建一个新会话（具体以 UI 为准）
  - 会话会自动出现在左侧列表中

### 6.2 发送消息

- 在输入框中输入中文或英文问题
- 点击「发送」或回车
- 右侧会话区域会先显示你的消息，然后 AI 答案以**流式输出**逐字出现

### 6.3 查看历史会话

- 点击左侧会话列表中的某个项
- 右侧会加载对应会话的完整历史消息
- Markdown 内容会做简单排版，代码块和列表会有基本格式

---

## 7. 常见问题（FAQ）

### 7.1 JavaFX 运行时报错：`java.lang.NoClassDefFoundError: javafx/...`

**可能原因：**

- 本地 JavaFX 运行环境未正确配置
- 直接用 `java -jar` 运行时未附带 JavaFX 模块

**建议解决方式：**

1. 优先使用 **IDE 运行配置**（IntelliJ IDEA 中运行 `Main` / `AiBotApplication`）
2. 确认 `pom.xml` 中已包含 JavaFX 依赖与运行插件
3. 如需命令行运行，请确保：
   - 使用 Maven 插件打包 JavaFX 可执行 JAR，或
   - 在 `java` 命令中显式添加 JavaFX 模块（根据你的 JDK/JavaFX SDK 配置）

> 具体命令与插件配置请参考项目的 `pom.xml` 及 Maven JavaFX 插件官方文档。

---

### 7.2 DeepSeek API 调用失败（超时 / 401 / 403 等）

**常见原因：**

- API Key 填写错误或已失效
- 访问地址配置错误（代理/网关错误）
- 网络环境不通（被防火墙 / 公司网络限制）

**排查步骤：**

1. 检查 `application.yml` 中的 `apiKey`、`baseUrl`、`model` 是否正确
2. 用 `curl` 或 Postman 测试同一机器对 DeepSeek API 的访问是否正常
3. 确认是否存在网络代理 / VPN / 公司网络限制

---

### 7.3 输出内容中的表情 / Emoji 乱码

**症状：**

- AI 回答中的表情符号显示为方框 `□` 或乱码

**可能原因：**

- 控制台 / JavaFX 控件 / 字体不完全支持 Emoji
- 字符编码或字体缺少 Emoji 字形

**建议解决方式：**

1. 确保应用整体使用 UTF-8 编码（Maven、IDE 编码统一为 UTF-8）
2. 在 JavaFX 中：
   - 尽量使用支持 Emoji 的字体
   - 或在 CSS（`src/main/resources/css/style.css`）中指定兼容的字体族
3. 如果个别表情仍无法显示，这是操作系统字体库的限制，可接受为正常现象

---

### 7.4 中文出现问号 `???` 或乱码

**可能原因：**

- 源文件编码不是 UTF-8
- 数据库连接或表的编码设置不一致（一般 SQLite UTF-8 问题较少，多为 UI/文件编码）

**建议解决方式：**

1. 确认 IDE 的项目编码为 UTF-8
2. 确认 Maven 编译参数中设置了 UTF-8（如 `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`）
3. 如果是数据库读取中文异常，检查 `DatabaseService` 中是否有特殊的编码转换逻辑

---

## 8. 项目结构简要说明

项目关键目录和类（基于当前结构）：

- `src/main/java/com/pei/`
  - `Main.java` / `AiBotApplication.java`：应用入口
  - `controller/ChatController.java`：JavaFX 控制器，处理 UI 事件和对话逻辑
  - `model/Conversation.java`、`model/Message.java`：会话与消息实体
  - `service/AiService.java`：封装与 DeepSeek API 的交互逻辑
  - `service/Config.java`：配置读取与管理
  - `service/DatabaseService.java`：SQLite 数据读写
  - `service/MarkdownTemplate.java`、`MarkdownUtil.java`：Markdown 渲染工具

- `src/main/resources/`
  - `application.yml`：应用配置（包括 DeepSeek API 等）
  - `fxml/chat-view.fxml`：JavaFX 界面布局
  - `css/style.css`：界面样式

- `aibot.db`：SQLite 数据库文件

---

## 9. 后续可扩展方向

以下是基于当前结构比较容易实现的扩展点，方便你或其他开发者继续迭代：

1. **多模型 / 多 API 支持**
   - 在 `Config` / `AiService` 中增加不同模型或供应商的配置（如切换 DeepSeek 模型、接入其他 API）
   - UI 增加下拉框选择当前对话使用的模型

2. **更完整的 Markdown 渲染**
   - 引入专业 Markdown 渲染库
   - 支持表格、任务列表、高亮代码等
   - 优化代码块字体和行高

3. **会话管理增强**
   - 支持重命名会话、删除会话
   - 支持根据关键字搜索历史对话
   - 增加「置顶」或「标签」功能

4. **导出与分享**
   - 支持将单个会话导出为 Markdown / HTML / PDF
   - 支持复制对话为带格式文本

5. **设置面板**
   - 提供 UI 界面修改：
     - DeepSeek API Key 与代理
     - 默认模型、温度、最大 Token 等参数
     - 主题（深色 / 浅色）、字体大小

6. **国际化（i18n）支持**
   - 在 FXML 与代码中抽取文案
   - 支持中英文切换

---

## 10. 许可证

本项目使用的许可证见根目录下的 `LICENSE` 文件。  
在使用或二次开发前，请仔细阅读许可证内容。

---

## 11. 反馈与贡献

欢迎提出 Issue 或提交 Pull Request：

- 如果你遇到 Bug 或使用问题：
  - 请附上运行环境（操作系统、JDK 版本）、异常栈信息和复现步骤
- 如果你有新的功能想法：
  - 可以在 Issue 中描述使用场景和期望行为，便于一起讨论设计方案

