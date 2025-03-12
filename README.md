# 交易管理系统

这是一个基于 Spring Boot 的交易管理系统，提供账户管理和交易处理功能。

## 功能特性

### 账户管理
- 创建账户
- 查询账户信息
- 更新账户信息
- 删除账户
- 更新账户余额

### 交易管理
- 创建交易
- 查询交易信息
- 查询交易列表
- 更新交易信息
- 删除交易



## API 接口

### 账户接口

```
POST   /api/accounts           - 创建新账户
GET    /api/accounts          - 获取所有账户
GET    /api/accounts/{id}     - 获取指定账户
PUT    /api/accounts/{id}     - 更新账户信息
DELETE /api/accounts/{id}     - 删除账户
PATCH  /api/accounts/{id}/balance - 更新账户余额
```

### 交易接口

```
POST   /api/transactions           - 创建新交易
GET    /api/transactions          - 获取所有交易
GET    /api/transactions/{id}     - 获取指定交易
PUT    /api/transactions/{id}     - 更新交易信息
DELETE /api/transactions/{id}     - 删除交易
```

## 快速开始

1. 克隆项目
```bash
git clone https://github.com/ZhangBobby/transaction_demo.git
```

2. 进入项目目录
```bash
cd transaction_demo
```

3. 构建项目
```bash
mvn clean install
```

4. 运行项目
```bash
mvn spring-boot:run
```

5. 访问接口
```
http://localhost:8080
```


### Docker 部署

1. 构建 Docker 镜像：
   ```bash
   docker build -t transaction-demo .
   ```
2. 运行容器：
   ```bash
   docker run -p 8080:8080 transaction-demo
   ```
   
 
## 测试

项目包含完整的单元测试和集成测试：

```bash
# 运行所有测试
mvn test

# 运行单个测试类
mvn test -Dtest=AccountControllerTest
```

### 测试报告
测试执行后，可以在 `target/surefire-reports` 目录下查看详细的测试报告。


## 注意事项

1. 账户余额不能为负数
2. 交易金额必须大于0
3. 转账时源账户必须有足够的余额
4. 已完成的交易不能修改 
