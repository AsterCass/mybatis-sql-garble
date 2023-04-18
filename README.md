# Mybatis数据拦截器: Garble

## 目前支持功能

1. 指定监控表数据更新返回更新行指定字段, 支持通过方法获取回调并且自定义后续处理
2. 支持数据查询鉴权功能, 目前支持的鉴权方法
   1. 同值鉴权: 通过自定义方法回调获取鉴权code, 将code和指定列比较相同即为有该权限
   2. 比特与鉴权: 通过自定义方法回调鉴权code, 将code和指定列比较比特位取与如果大于0即为有该权限
   3. 交集鉴权, 传入String形式的List, 如果数据对应字段有一个想匹配则有该权限
3. 支持数据插入自动写入权限
4. 支持数据更新鉴权
5. 其他
   1. 兼容page-helper测试完成
   2. 兼容不同schema相同数据表名的监控

## 日程中功能

1. 支持联表更新的数据鉴权
2. 测试与mybatis plus等其他拦截器的兼容性
3. 获取鉴权方法使用正则匹配代替原本的list参数，简化多数据表的鉴权的配置成本
4. 识别@select和@update等注解和实际的sql是否匹配，目前不匹配也可以运行，但是会造成鉴权配置紊乱（select会读update的配置之类）


## 挂起的功能
1. 交集鉴权支持数据单个列存jsonList的情况, 目前仅支持传入的权限写为List
> __Note__
> 麻烦再议
2. 支持多字段鉴权（多字段同时满足对应的鉴权方法才能鉴权成功, 授权也可以同时授权多个字段） 
> __Note__
> 更麻烦，再议
3. 支持在update语句中添加select子查询的查询鉴权
> __Note__
> 目前在update中的select子查询的语句鉴权是采用了update中配置的鉴权策略而不是select中的
> 如果两者配置相同，那么则没有问题，如果update语句中select子查询需要使用select的鉴权配置需要跨配置域操作，比较麻烦
4. 使用更新/插入/查询鉴权可以通过继承不同的方法实现配置不同获取鉴权数据的目的，
而不是通过在注解中配置type的方式，减少配置项，同时支持默认获取鉴权数据的方法
> __Note__
> 这个不重要，再议

## 快速开始

以使用【返回更新数据】功能为例，其他功能参考下方【功能简述】

### 数据表搭建

引入该功能需要增加一个数据库字段来标明更新状态, 具体增加字段的字段名需要在配置文件中指定

执行统计文件下的**env.sql**文件搭建测试数据环境

### 配置

根据项目选用spring boot配置还是maven配置, gradle同理

如果使用的是springboot构建项目使用springboot配置即可，不需要再搭建maven配置

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
      #默认更新标记字段
      default-flag-col-name: "update_record"
      #监控表和回调字段的对应关系，一般为主键
      monitored-table-map: { 'garble_task': 'id' }
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
         <property name="updated-data-msg.monitoredTableMap" value="{'garble_task':'id'}"/>
      </plugin>
   </plugins>
</configuration>
```

### 代码

代码部分以springboot项目为例，maven项目类似，就不展示了，如果有需要可以移步本项目的测试目录查看

#### 更新监控表

##### service:

```java
public class UpdateCallbackTest {

   @Resource
   private GarbleTaskMapper garbleTaskMapper;

   public void simpleUpdateCallbackTest() {
      garbleTaskMapper.updateOne();
   }

}
```

##### mapper:

```java

@org.apache.ibatis.annotations.Mapper
public interface GarbleTaskMapper extends Mapper<GarbleTask> {

   @Update("update garble_task set t_name = '工作xx' where e_id = 55")
   void updateOne();

}
```

##### callback:

回调函数: 当感知到user监控表被更新的时候, 会回调该函数让用户可以感知到数据表的变化

如果为maven项目则不需要@Service注解

```java
@Service
public class UpdatedOneService implements DealWithUpdatedInterface {

   /**
    * 回调方法需要是实现DealWithUpdatedInterface, 并且需要通过@DealWithUpdated注解
    * 标明优先级, 如果存在多个继承DealWithUpdatedInterface的类，优先级priority更小的会更先执行
    */
   @DealWithUpdated(priority = 1)
   @Override
   public void execute(Map<String, List<String>> updatedTableMap) {
      System.out.println(JSON.toJSONString(updatedTableMap));
   }
}
```

##### log:

此时可以观测到日志信息

```text
{"garble_task":["7"]}
```

## 全功能完整配置介绍

这里只展示yml文件的配置, 如果不使用springboot则需要配置mybatis-config,
字段和yml配置相同, 但是需要增加功能前缀, 参考【快速开始】-【配置】-【maven配置】或者参考本项目的测试目录

这里为完整配置的说明, 不需要全部都赋值

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
      #更新鉴权
      update:
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
```

## 注意事项

1. 目前只支持mysql
2. 目前要求数据权限列必须要有权限标识,如果权限标识为null意味着该行不会在任何情况下被检索到,
   如果需要跳出权限校验使用excluded-mapper-path配置
3. 无论是sql-garble-spring-boot还是mybatis-sql-garble中的@Test部分
   测试用例相关代码中为了尽可能覆盖更多逻辑，部分地方配置有冗余、不够简便，需要用户注意区隔
4. **【important】mybatis的\<select\>标签和mapper文件中@Select标识不要写错, 在正常情况下可能没有影响, 
但是在鉴权逻辑中 @Select @Update这种标识是区分鉴权方式的 会导致鉴权配置紊乱的问题**


## 功能简述

### 返回更新数据

使用方法参照【快速开始】, 这里简述一下原理

使用拦截器拦截含监控表经过mybatis的相关sql, 增加set条件update_recode = 1 (update_record 字段可以在配置中自定义)
此时会把所有更新行的update_record设置为1

然后在mybatis执行完成后再次拦截, update_record = 1 的数据统计起来, 执行查询sql获取指定列

最后将之前行重置回 update_record = 0, 并且找到继承 DealWithUpdatedInterface 的方法回调将跟新行返回

也就是说在更新监控表内执行sql, 其实相当于执行了3此sql, 一次查询以及两次更新,
如果希望提高更新效率将monitored-table-map的value值配置为主键，或者对该字段构造索引

### 数据查询鉴权

依然是使用env.sql中的测试数据

在满足**文件配置完成**且**数据表有鉴权列**后, 还需要继承AuthenticationCodeInterface
实现authenticationCodeBuilder方法以及增加@AuthenticationCodeBuilder注解.
在该功能在配置文件中被声明需要时, 继承AuthenticationCodeInterface的文件被扫描,
查询实时获取鉴权code, 根据用户定义策略方法进行匹配

```java
import com.aster.plugin.garble.enums.AuthenticationStrategyEnum;

@Service
public class AuthTaskService implements AuthenticationCodeInterface {


   /**
    * 后去权限code，用于和配置字段相比较
    * {@link AuthenticationStrategyEnum}
    * <p>
    * 如果使用的是AuthenticationStrategyEnum.BOOLEAN_AND需要传入的为纯数字的字符串
    * <p>
    * 如果使用的是AuthenticationStrategyEnum.INTERSECTION 需要传入的为可解析的ListString类型的字符串,
    * 数据需要为ListString或者单独的String字符串
    * <p>
    * 方法将会在查询监控表的时候依据指定的及authentication strategy，根据此方法的的返回值进行鉴权
    *
    * @return 鉴权code
    */
   @Override
   @AuthenticationCodeBuilder(type = 2, tables = {"^.*garble_task$"})
   public String authenticationCodeBuilder() {
      return JSON.toJSONString(Collections.singletonList("123"));
   }
}
```

这里展示使用交集鉴权的配置

```yaml
garble:
   valid: true
   garble-function-list:
      - 2
   #鉴权
   auth:
      #查询鉴权
      select:
         #监控表列表
         monitored-table-list:
            - "garble_task"
         #监控表的默认权限标记列
         default-auth-col-name: "auth_code_col"
         #监控表和权限策略
         default-auth-strategy: 3
```

数据库状态

```text
id|e_id|t_name|update_record|auth_code_col|
--+----+------+-------------+-------------+
 1|  11|工作1   |            0|123          |
 2|  11|工作2   |            0|1234         |
 3|  11|工作3   |            0|123          |
 4|  22|工作4   |            0|123          |
 5|  22|工作5   |            0|123          |
 6|  44|工作6   |            0|123          |
 7|  55|工作7   |            0|123          |
 8|  66|工作8   |            0|123          |
 9|  77|工作9   |            0|123          |
10|  77|工作9   |            0|123          |
11|  77|工作9   |            0|1234         |
12|  77|工作9   |            0|1234         |
13|  88|工作10  |            0|1234         |
14|  88|工作10  |            0|123          |
15|  88|工作10  |            0|123          |
16|  99|工作11  |            0|123          |
17|1010|工作12  |            0|123          |
18|1212|工作13  |            0|123          |
19|1313|工作14  |            0|123          |
20|1414|工作15  |            0|123          |
21|1515|工作16  |            0|123          |
22|1515|工作17  |            0|123          |
23|1515|工作18  |            0|123          |
24|1515|工作19  |            0|123          |
```

查询全部数据

```java
import tk.mybatis.mapper.common.Mapper;

@org.apache.ibatis.annotations.Mapper
public interface GarbleTaskMapper extends Mapper<GarbleTask> {
}
```

```java
public class AuthSelectTest {

   @Resource
   private GarbleTaskMapper garbleTaskMapper;
   
    @Test
    public void test() {
       List<GarbleTask> allAuthTaskList = garbleTaskMapper.selectAll();
       Assert.assertNotNull(allAuthTaskList);
       Assert.assertEquals(allAuthTaskList.size(), 20);
   }
}
```

sql log

```text
Preparing: SELECT id, e_id, t_name, update_record, auth_code_col FROM garble_task WHERE garble_task.auth_code_col IN ('123')
Parameters: 
Total: 20
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

