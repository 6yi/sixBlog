# sixBlog
##### demo地址 :59.110.173.180:8082

### 获取所有博文标题

>/title

##### 示例

localhost:8080/title

##### 返回格式

```json
{
  "msg": "succeed",
  "length": 2,
  "state": 200,
  "titles": [
    {
      "title": "java",
      "title_id": 1,
      "t_id": 1,
      "a_id": 1,
      "created": "2020-04-10T13:43:20.000",
      "text": "牛逼噢"
    },
    {
      "title": "vertx",
      "title_id": 2,
      "t_id": 1,
      "a_id": 2,
      "created": "2020-04-10T13:43:33.000",
      "text": "哈哈"
    }
  ]
}
```

##### 说明: t_id为博文类型,a_id为博文内容,text为博文展示的短内容,created为创建时间





### 通过a_id获取博文

#### (获取请必须先获取到标题,标题里包含a_id)

>/articleByAid/:id

##### 示例

localhost:8080/articleByAid/1

##### 返回格式

```json
{
  "msg": "succeed",
  "state": 200,
  "article": {
    "a_id": 1,
    "t_id": 1,
    "created": "2020-04-10T15:01:56.000",
    "text": "​\tJVM类加载分为三个部分\r\n\r\n>加载\r\n>\r\n>连接\r\n>\r\n>初始化\r\n\r\n### 加载\r\n\r\n加载过程主要进行了三个操作\r\n\r\n1.通过类的全限定类名来获取该类的二进制字节类\r\n\r\n2.将字节类的静态存储结构转为方法区的运行时数据结构\r\n\r\n3.在堆中生成此类的 **jav......."
  }
}
```

##### text数据为md格式,请在前端用md解析库来解析



### 获取博文类型

>/type

##### 示例

localhost:8080/type

##### 返回格式

```json
{
  "msg": "succeed",
  "state": 200,
  "types": [
    {
      "t_id": 1,
      "t_name": "java"
    },
    {
      "t_id": 2,
      "t_name": "linux"
    }
  ] 
}
```



### 通过类型获取博文标题

>/title/:t_id

##### 示例

localhost:8080/title/1

##### 返回格式

```json
{
  "msg": "succeed",
  "length": 2,
  "state": 200,
  "titles": [
    {
      "title": "java",
      "title_id": 1,
      "t_id": 1,
      "a_id": 1,
      "created": "2020-04-10T13:43:20.000",
      "text": "牛逼噢"
    },
    {
      "title": "vertx",
      "title_id": 2,
      "t_id": 1,
      "a_id": 2,
      "created": "2020-04-10T13:43:33.000",
      "text": "哈哈"
    }
  ]
}
```



### 通过a_id获取评论

>/comment

##### 示例

localhost:8080/comment/1

##### 返回格式

```json
{
  "msg": "succeed",
  "comments": [
    {
      "c_id": 1,
      "a_id": 1,
      "text": "Good",
      "created": "2020-04-10T16:51:03.000",
      
    },
    {
      "c_id": 2,
      "a_id": 1,
      "text": "66666666666666",
      "created": "2020-04-10T18:00:44.000",
     
    }
  ],
  "state": 200
}
```





### 获取失败

##### 返回格式

```json
{
  "state": 0,
  "msg": "数据库连接失败"
}
```

