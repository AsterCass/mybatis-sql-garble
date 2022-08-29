# Mybatis数据拦截器: Garble

## 目前支持功能

1. 指定监控表数据更新返回更新行指定字段, 支持通过方法获取回调并且自定义后续处理
2. 支持数据查询鉴权功能, 目前支持的鉴权方法
   1. 同值鉴权: 通过自定义方法回调获取鉴权code, 将code和指定列比较相同即为有该权限
   2. 比特与鉴权: 通过自定义方法回调鉴权code, 将code和指定列比较比特位取与如果大于0即为有该权限

## 日程中功能

1. 支持数据插入自动写入权限
2. 支持数据更新鉴权
3. 支持第三种鉴权方式, 交集鉴权, 传入String形式的List, 数据库中也为String形式的List, 如果有重合, 即为有权限
4. 兼容不同schema相同数据表名的监控, 以及对于不同的schema的功能完整性测试

## 快速开始（以使用【返回更新数据】功能为例）

### 数据表搭建

加入项目需要增加col, 这里使用 update_record 为例, 具体增加字段可以在配置文件中指定

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

### 配置

#### spring boot 配置

1.配置
pom.xml

```xml

<dependencies>
   <dependency>
      <groupId>com.astercasc</groupId>
      <artifactId>mybatis-sql-garble</artifactId>
      <version>${version}</version>
   </dependency>
   <dependency>
      <groupId>org.mybatis.spring.boot</groupId>
      <artifactId>mybatis-spring-boot-starter</artifactId>
      <version>2.2.2</version>
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

#### maven

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

这里只展示yml文件的配置, mybatis-config 的配置相同, 但是需要增加功能前缀, 参考【快速开始】-【配置】

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
```

## 注意事项

1. 目前只支持mysql
2. 对于多schema的场景需要完善

## 功能简述

### 返回更新数据

### 数据查询鉴权

### 数据更新鉴权

### 数据插入授权