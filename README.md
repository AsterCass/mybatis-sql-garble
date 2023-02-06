# Mybatis数据拦截器: Garble

## 目前支持功能

1. 指定监控表数据更新返回更新行指定字段, 支持通过方法获取回调并且自定义后续处理
2. 支持数据查询鉴权功能, 目前支持的鉴权方法
   1. 同值鉴权: 通过自定义方法回调获取鉴权code, 将code和指定列比较相同即为有该权限
   2. 比特与鉴权: 通过自定义方法回调鉴权code, 将code和指定列比较比特位取与如果大于0即为有该权限
   3. 交集鉴权, 传入String形式的List, 如果数据对应字段有一个想匹配则有该权限
3. 支持数据插入自动写入权限
4. 其他
   1. 兼容page-helper测试完成

## 日程中功能

1. 支持数据更新鉴权
2. 支持联表更新的数据鉴权
3. 兼容不同schema相同数据表名的监控, 以及对于不同的schema的功能完整性测试, 需要重构对于表名的处理逻辑
4. 交集鉴权支持数据单个列存jsonList的情况, 目前仅支持传入的权限写为List
5. 支持多字段鉴权（多字段同时满足对应的鉴权方法才能鉴权成功, 授权也可以同时授权多个字段）
6. 支持在update语句中添加select子查询的查询鉴权
7. 测试与mybatis plus等其他拦截器的兼容性
8. 创建时间和更新时间自动更新
9. 使用更新/插入/查询鉴权可以通过继承不同的方法实现配置不同获取鉴权数据的目的，
而不是通过在注解中配置type的方式，减少配置项，同时支持默认获取鉴权数据的方法
10. 获取鉴权方法使用正则匹配代替原本的list参数，简化多数据表的鉴权的配置成本
11. 用户提示，mybatis的\<select\>标签和mapper文件中@Select标识不要写错, 在正常情况下可能没有影响,
但是在鉴权逻辑中 @Select @Update这种标识是区分鉴权方式的 会导致鉴权配置紊乱的问题


## 快速开始（以使用【返回更新数据】功能为例）

### 数据表搭建

引入该依赖需要增加希望监控表的数据列,
这里使用 update_record 为例, 具体增加字段名可以在配置文件中指定

```sql
CREATE DATABASE `garble` DEFAULT CHARACTER SET utf8mb4;

USE `garble`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user`
(
   `id`            int(11) NOT NULL AUTO_INCREMENT,
   `name`          varchar(50) DEFAULT NULL,
   `ext`           varchar(50) DEFAULT NULL,
   `update_record` int(4)      DEFAULT 0,
   PRIMARY KEY (`Id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 9
  DEFAULT CHARSET = utf8mb4;

insert into user(id, name, ext)
values ('1', '张老大', 'aaa');
insert into user(id, name, ext)
values ('2', '张老二', 'bbb');
insert into user(id, name, ext)
values ('3', '张老三', 'ccc');
insert into user(id, name, ext)
values ('4', '张老四', 'ddd');
insert into user(id, name, ext)
values ('5', '张老五', 'eee');
insert into user(id, name, ext)
values ('6', '张老六', 'fff');
```

### 配置（根据项目选用spring boot配置还是maven配置, gradle同理）

#### spring boot 配置

1.配置
pom.xml

```xml

<dependencies>
   <dependency>
      <groupId>com.astercasc</groupId>
      <artifactId>sql-garble-spring-boot-starter</artifactId>
      <version>${version}</version>
   </dependency>
   <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>${version}</version>
   </dependency>
</dependencies>
```

application.yml

```yaml
garble:
   #是否开启拦截器
   valid: true
   #拦截器所含功能 GarbleFunctionEnum
   garble-function-list:
      - 1
   #【返回更新数据】功能所需配置
   updated-data-msg:
      #默认更新标记字段, 如果监控表的更新标记字段没有在monitored-table-update-flag-col-map找到对应关系, 会取该值
      default-flag-col-name: "update_record"
      #监控表和回调字段的对应关系，一般为主键
      monitored-table-map: { 'user_table': 'id', 'log_table': 'id' }

```

#### maven配置

pom.xml

```xml

<dependencies>
   <dependency>
      <groupId>com.astercasc</groupId>
      <artifactId>mybatis-sql-garble</artifactId>
      <version>${version}</version>
   </dependency>
   <dependency>
      <groupId>org.mybatis</groupId>
      <artifactId>mybatis</artifactId>
      <version>${version}</version>
   </dependency>
</dependencies>
```

mybatis-config.xml

```xml

<configuration>
   <plugins>
      <plugin interceptor="com.aster.plugin.garble.interceptor.GarbleUpdateInterceptor">
         <!--updated-data-msg 前缀标识父配置, 即为使用的功能, 参考GarbleFunctionEnum, 不希望使用的功能不要加载任何相关的子配置-->
         <!--默认更新标记字段, 如果监控表的更新标记字段没有在monitoredTableUpdateFlagColMap找到对应关系, 会取该值-->
         <property name="updated-data-msg.defaultFlagColName" value="update_record"/>
         <!--监控表和回调字段的对应关系，一般为主键-->
         <property name="updated-data-msg.monitoredTableMap" value="{'user':'id'}"/>
      </plugin>
   </plugins>
</configuration>
```

### 代码

#### 更新监控表

service:

```java
public class BaseTest {
   public void test() {
      //get userMapper...
      userMapper.updateOne("张老二", "bbb");
   }
}
```

mapper:

```java
public interface UserMapper {
   int updateOne(@Param("name") String name, @Param("ext") String ext);
}
```

xml:

```xml

<update id="updateOne">
   update user set name = #{name} where ext = #{ext}
</update>
```

回调函数: 当感知到user监控表被更新的时候, 会回调该函数让用户可以感知到数据表的变化

callback:

```java
@Service
public class UpdatedOneService implements DealWithUpdatedInterface {

   /**
    * 回调方法需要是实现DealWithUpdatedInterface, 并且需要通过@DealWithUpdated注解
    * 标明优先级, 优先级priority更小的会更先执行
    */
   @DealWithUpdated(priority = 1)
   @Override
   public void execute(Map<String, List<String>> updatedTableMap) {
      System.out.println("1:" + JSON.toJSONString(updatedTableMap));
   }
}
```

```java
@Service
public class UpdatedTwoService implements DealWithUpdatedInterface {

   /**
    * 回调方法需要是实现DealWithUpdatedInterface, 并且需要通过@DealWithUpdated注解
    * 标明优先级, 优先级priority更小的会更先执行
    */
   @DealWithUpdated(priority = 2)
   @Override
   public void execute(Map<String, List<String>> updatedTableMap) {
      System.out.println("2:" + JSON.toJSONString(updatedTableMap));
   }
}
```

log:

```text
1:{"user":["2"]}
2:{"user":["2"]}
```

## 配置介绍

这里只展示yml文件的配置, 如果不使用 spring boot 则需要配置 mybatis-config , 字段和yml配置相同, 但是需要增加功能前缀, 参考【快速开始】-【配置】

```yaml
garble:
   #是否开启拦截器
   valid: true
   #拦截器所含功能 GarbleFunctionEnum
   garble-function-list:
      - 1
   #【返回更新数据】功能所需配置
   updated-data-msg:
      #忽略的拦截路径, 即使是在监控表中, 如是忽略拦截路径中sql请求, 不会监控它的更新, 也不会回调
      excluded-mapper-path:
         - "com.aster.mapper.ExcludeMapper"
      #默认更新标记字段, 如果监控表的更新标记字段没有在monitored-table-update-flag-col-map找到对应关系, 会取该值
      default-flag-col-name: "update_record"
      #回调方法的的路径, 增加项目启动速度, 可以不填
      dealWithUpdatedPath: "com.baidu"
      #监控表和回调字段的对应关系，一般为主键
      monitored-table-map: { 'user_table': 'id', 'log_table': 'id' }
      #监控表和更新标记字段的对应关系
      monitored-table-update-flag-col-map: { 'user_table': 'update_record_col1', 'log_table': 'update_record_col2' }
   #鉴权   
   auth:
      #查询鉴权
      select:
         #标记实现AuthenticationCodeInterface接口的方法路径，加快加快初始化速度，可以不赋值
         auth-code-path: "com.baidu"
         #监控表列表
         monitored-table-list:
            - "user"
         #监控表和权限标记列
         monitored-table-auth-col-map: { 'user_table': 'auth_code_o', 'log_table': 'auth_code_t' }
         #监控表的默认权限标记列，当monitored-table-update-flag-col-map无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
         default-auth-col-name: "auth_code"
         #监控表和权限策略
         monitored-table-auth-strategy-map: { 'user_table': 1, 'log_table': 2 }
         #监控表和权限策略，当monitored-table-auth-strategy-map无法查询到需要监控表的权限策略的时候，使用默认权限测率
         default-auth-strategy: 1
         #在此map中的的sql不受到监控，即使包含监控表
         excluded-mapper-path:
            - "com.aster.mapper.ExcludeMapperRed"
      #更新授权
      insert:
         #标记实现AuthenticationCodeInterface接口的方法路径，加快加快初始化速度，可以不赋值
         auth-code-path: "com.baidu"
         #监控表列表
         monitored-table-list:
            - "user"
         #监控表和权限标记列
         monitored-table-auth-col-map: { 'user_table': 'auth_code_o', 'log_table': 'auth_code_t' }
         #监控表的默认权限标记列，当monitored-table-update-flag-col-map无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
         default-auth-col-name: "auth_code"
         #在此map中的的sql不受到监控，即使包含监控表
         excluded-mapper-path:
            - "com.aster.mapper.ExcludeMapperRed"
```

## 注意事项

1. 目前只支持mysql
2. 对于多schema的场景需要完善
3. 目前要求数据权限列必须要有权限标识,如果权限标识为null意味着该行不会在任何情况下被检索到
4. 回调函数中的@Service是spring boot项目中的需要增加的, 如果是非spring项目直接引入mybatis-sql-garble包的话则不需要添加

## 功能简述

### 返回更新数据

使用方法参照【快速开始】, 这里简述一下原理

使用拦截器拦截含监控表经过mybatis的相关sql, 增加set条件update_recode = 1 (update_record 字段可以在配置中自定义)
此时会把所有更新行的update_record设置为1

然后在mybatis执行完成后再次拦截, update_record = 1 的数据统计起来, 执行查询sql获取指定列

最后将之前行重置回 update_record = 0, 并且找到继承 DealWithUpdatedInterface 的方法回调将跟新行返回

也就是说在更新监控表内执行sql, 其实相当于执行了3此sql, 一次查询以及两次更新,
如果希望提高更新效率首先将monitored-table-map的value值配置为主键, 再者依据update_record建立索引

### 数据查询鉴权

```sql
CREATE TABLE your_schema.your_table
(
   `id` int(11) NOT NULL AUTO_INCREMENT,
   -- your cols
   PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


ALTER TABLE your_schema.your_table
   ADD your_auth_code varchar(50) NULL COMMENT '权限标识code';
```

在配置文件配置完成, sql列增加完成后, 还需要继承AuthenticationCodeInterface
实现authenticationCodeBuilder方法以及增加@AuthenticationCodeBuilder注解.
在该功能在配置文件中被声明需要时, 继承AuthenticationCodeInterface的文件被扫描,
查询实时获取鉴权code, 根据用户定义策略方法进行匹配

```java
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;

@Service
public class QueryAuthService implements AuthenticationCodeInterface {
   /**
    * 获取鉴权code，用于和配置字段相比较
    * {@link AuthenticationStrategyEnum}
    * 如果使用的是AuthenticationStrategyEnum.BOOLEAN_AND需要传入的为纯数字的字符串
    * 方法将会在查询监控表的时候依据指定的及authentication strategy，根据此方法的的返回值进行鉴权
    *
    * @return 鉴权code
    */
   @Override
   @AuthenticationCodeBuilder(type = 2, tables = {"user"})
   public String authenticationCodeBuilder() {
      return "12345";
   }
}
```

这里展示使用等式鉴权的配置

```yaml
garble:
   #是否开启拦截器
   valid: true
   #拦截器所含功能 GarbleFunctionEnum
   garble-function-list:
      - 2
   #鉴权   
   auth:
      #查询鉴权
      select:
         #监控表列表
         monitored-table-list:
            - "user"
         #监控表的默认权限标记列，当monitored-table-update-flag-col-map无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
         default-auth-col-name: "auth_code"
         #监控表和权限策略，当monitored-table-auth-strategy-map无法查询到需要监控表的权限策略的时候，使用默认权限测率
         default-auth-strategy: 1
```

数据库状态

```text
id |name|ext|update_record|auth_code|
---+----+---+-------------+---------+
  1|张老大 |11 |            0|         |
  2|张老二 |22 |            0|12345    |
  3|张老三 |33 |            0|         |
  4|张老四 |44 |            0|         |
  5|张老五 |55 |            0|         |
  6|张老六 |66 |            0|         |
  7|张老七 |77 |            0|         |
  8|张老八 |88 |            0|         |
```

查询全部数据

```java
public class BaseTest {
   public void test() {
      //get userMapper...
      List<UserEntity> list = userMapper.selectAll();
      System.out.println(JSON.toJSONString(list));
   }
}
```

结果

```text
[{"ext":"22","id":2,"name":"张老二"}]
```

### 数据更新鉴权

### 数据插入授权

测速数据库配置信息和【数据查询鉴权】相同, 同样需要继承AuthenticationCodeInterface,
实现authenticationCodeBuilder, type 使用 AuthenticationTypeEnum.INSERT 表明这个是插入的获取权限的方法,
tables 为作用域, 需要保证在auth.insert配置文件中的每个table都有自己的作用域

```java
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;
import com.aster.plugin.garble.service.AuthenticationCodeBuilder;
import com.aster.plugin.garble.service.AuthenticationCodeInterface;

@Service
public class InsertAuthService implements AuthenticationCodeInterface {


   /**
    * 获取鉴权code，用于和配置字段相比较
    * {@link AuthenticationStrategyEnum}
    * 如果使用的是AuthenticationStrategyEnum.BOOLEAN_AND需要传入的为纯数字的字符串
    * 方法将会在查询监控表的时候依据指定的及authentication strategy，根据此方法的的返回值进行鉴权
    *
    * @return 鉴权code
    */
   @Override
   @AuthenticationCodeBuilder(type = 3, tables = {"user"})
   public String authenticationCodeBuilder() {
      return "1234";
   }
}

```

这里展示测试的yml配置

```yaml
garble:
   #是否开启拦截器
   valid: true
   #拦截器所含功能 GarbleFunctionEnum
   garble-function-list:
      - 3
   #鉴权   
   auth:
      #查询鉴权
      select:
         #监控表列表
         monitored-table-list:
            - "user"
         #监控表的默认权限标记列，当monitored-table-update-flag-col-map无法查询到需要监控表的权限标记列的时候，使用默认权限标记列
         default-auth-col-name: "auth_code"
```

此时插入所有监控表数据都会增加对于权限字段的授权譬如

```text
insert into user (id, `name`, ext) values (123,  'szss', 'sss')
->
insert into user (id, `name`, ext, auth_code) values (123,  'szss', 'sss', '1234')
```

