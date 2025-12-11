# CORS 配置测试指南

## 已完成的修复

### 1. 更新了 SecurityConfig.java
- 使用 `addAllowedOriginPattern("*")` 支持所有来源
- 使用 `addAllowedMethod("*")` 支持所有 HTTP 方法
- 使用 `addAllowedHeader("*")` 支持所有请求头
- 设置 `setAllowCredentials(true)` 允许携带凭证
- 添加了多个暴露的响应头

### 2. 创建了自定义 CorsFilter.java
- 在最高优先级执行，确保所有响应都包含 CORS 头部
- 动态设置 `Access-Control-Allow-Origin` 头
- 自动处理 OPTIONS 预检请求

### 3. 创建了健康检查接口
- `/api/health` - 基本健康检查
- `/api/health/cors-test` - CORS 专用测试接口

## 测试步骤

### 方法 1：使用浏览器开发者工具测试

1. 打开浏览器开发者工具（F12）
2. 进入 Console 标签
3. 运行以下代码测试 CORS：

```javascript
// 测试 GET 请求
fetch('http://localhost:18080/api/health/cors-test', {
    method: 'GET',
    headers: {
        'Content-Type': 'application/json'
    },
    credentials: 'include'  // 如果需要携带凭证
})
.then(response => response.json())
.then(data => console.log('成功:', data))
.catch(error => console.error('错误:', error));

// 测试 POST 请求（登录接口）
fetch('http://localhost:18080/api/auth/login', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        username: 'your_username',
        password: 'your_password'
    }),
    credentials: 'include'
})
.then(response => response.json())
.then(data => console.log('登录成功:', data))
.catch(error => console.error('登录错误:', error));
```

### 方法 2：使用 curl 测试

```bash
# 测试预检请求（OPTIONS）
curl -X OPTIONS http://localhost:18080/api/health/cors-test \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Content-Type" \
  -v

# 测试实际请求
curl -X GET http://localhost:18080/api/health/cors-test \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -v
```

### 方法 3：使用 Postman 测试

1. 打开 Postman
2. 创建新请求：GET `http://localhost:18080/api/health/cors-test`
3. 在 Headers 中添加：
   - `Origin: http://localhost:3000`
   - `Content-Type: application/json`
4. 发送请求
5. 检查响应头中是否包含：
   - `Access-Control-Allow-Origin`
   - `Access-Control-Allow-Credentials`
   - `Access-Control-Allow-Methods`

## 检查响应头

成功的 CORS 响应应该包含以下头部：

```
Access-Control-Allow-Origin: http://localhost:3000 (或你的前端地址)
Access-Control-Allow-Credentials: true
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD
Access-Control-Allow-Headers: Origin, X-Requested-With, Content-Type, Accept, Authorization, ...
Access-Control-Expose-Headers: Authorization, Content-Type, X-Total-Count, ...
Access-Control-Max-Age: 3600
```

## 常见问题排查

### 1. 如果仍然出现 CORS 错误

检查浏览器控制台的具体错误信息：

**错误1**: `Access to fetch at 'xxx' from origin 'xxx' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header`
- **原因**: 响应中缺少 CORS 头部
- **解决**: 确认后端已重启，CorsFilter 已生效

**错误2**: `Access to fetch at 'xxx' from origin 'xxx' has been blocked by CORS policy: The value of the 'Access-Control-Allow-Origin' header in the response must not be the wildcard '*' when the request's credentials mode is 'include'`
- **原因**: 使用凭证时不能用通配符
- **解决**: CorsFilter 会自动处理，动态设置具体的 Origin

**错误3**: `Access to fetch at 'xxx' from origin 'xxx' has been blocked by CORS policy: Response to preflight request doesn't pass access control check`
- **原因**: OPTIONS 预检请求未正确处理
- **解决**: 已在 JwtAuthenticationFilter 和 CorsFilter 中处理

### 2. 前端配置建议

如果使用 axios：

```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:18080/api',
  withCredentials: true,  // 允许携带凭证
  headers: {
    'Content-Type': 'application/json'
  }
});
```

如果使用 fetch：

```javascript
fetch('http://localhost:18080/api/xxx', {
  method: 'GET',
  credentials: 'include',  // 允许携带凭证
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token  // 如果需要
  }
});
```

### 3. 生产环境建议

在生产环境中，应该限制允许的来源：

修改 `SecurityConfig.java` 中的 CORS 配置：

```java
// 仅允许特定来源
configuration.addAllowedOriginPattern("https://yourdomain.com");
configuration.addAllowedOriginPattern("https://www.yourdomain.com");
```

或在 `application.yml` 中配置：

```yaml
cors:
  allowed-origins:
    - https://yourdomain.com
    - https://www.yourdomain.com
```

## 重启应用

修改配置后，请重启应用：

```bash
# 如果使用 Gradle
./gradlew bootRun

# 或者直接在 IDE 中重启
```

## 验证清单

- [ ] 后端应用已成功重启
- [ ] 浏览器控制台没有 CORS 错误
- [ ] OPTIONS 预检请求返回 200
- [ ] 实际请求能够成功获取数据
- [ ] 响应头中包含必要的 CORS 头部
- [ ] 可以成功携带 Authorization 头部

## 需要帮助？

如果仍然有问题，请提供：
1. 浏览器控制台的完整错误信息
2. 前端请求的完整代码
3. 前端运行的地址（如 http://localhost:3000）
4. Network 标签中的请求和响应详情

