# 后端项目设置指南

## 首次设置

### 1. 下载 Gradle Wrapper

如果 `gradle/wrapper/gradle-wrapper.jar` 文件不存在，需要先下载：

**方法一：使用已安装的 Gradle**
```bash
gradle wrapper
```

**方法二：手动下载**
访问 https://services.gradle.org/distributions/gradle-8.5-bin.zip 下载，然后解压。

**方法三：使用项目提供的脚本**
```bash
# Windows PowerShell
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
```

### 2. 验证设置

运行以下命令验证项目设置：
```bash
# Windows
gradlew.bat --version

# Linux/Mac
./gradlew --version
```

### 3. 构建项目

```bash
# Windows
gradlew.bat build

# Linux/Mac
./gradlew build
```

### 4. 运行项目

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

## 常见问题

### Q: 找不到 gradle-wrapper.jar
A: 运行 `gradle wrapper` 命令生成 wrapper 文件。

### Q: 端口 8080 已被占用
A: 修改 `src/main/resources/application.yml` 中的 `server.port` 配置。

### Q: H2 数据库连接失败
A: 确保 H2 数据库依赖已正确下载，检查 `build.gradle` 中的依赖配置。
