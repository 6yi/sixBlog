package tk.lzheng;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


public class BlogVerticle extends AbstractVerticle {
    static final String articleSqlByAid="select * from myblog_article where a_id=? and viewable=1";
    static final String titleSqlByTid="select * from myblog_title where t_id=?";
    static final String commentSqlByAid="select * from myblog_comment where a_id=?";
    static final String addComment="INSERT myblog_comment(a_id,`name`,`text`,created,`view`) VALUES(?,?,?,?,?)";
    AsyncSQLClient sqlClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer httpServer= vertx.createHttpServer();
        Router router = Router.router(vertx);
        new Mysqlclient(vertx, config());

        //跨域解决

        router.route().handler(CorsHandler.create("*")
                .allowedHeader("x-csrftoken")
                .allowedHeader("Access-Control-Allow-Headers")
                .allowedHeader("Access-Control-Allow-Origin")
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.OPTIONS));

        router.route().handler(CookieHandler.create());
        SessionStore store = LocalSessionStore.create(vertx);
        router.route().handler(SessionHandler.create(store));

        router.get("/title").handler(context->{
                Mysqlclient.sqlClient.getConnection(res->{
                    hand(res, context, h->{
                        res.result().query("SELECT *,DATE_FORMAT(created, '%Y-%m-%d') AS create_time FROM myblog_title where viewable=1",result->{
                            List<JsonObject> jsonObject = result.result().getRows();
                            Map<String,Object> rtMap=reValue(200, "succeed", jsonObject.size(), jsonObject);
                            res.result().close();
                            context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                        });
                    });
                });
        });
        //INSERT myblog_comment(a_id,name,`text`,created,view) VALUES(?,?,?,?)
        router.post("/addComment").handler(BodyHandler.create()).handler(context->{
            String json="";
            try {
              json= URLDecoder.decode(context.getBodyAsString(),"UTF-8").split("=")[1];
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JsonObject jsonObject = new JsonObject(json);
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    SimpleDateFormat sdf =new SimpleDateFormat("YYYY-MM-dd HH:MM:ss" );
                    Date d= new Date();
                    String str = sdf.format(d);
                    JsonArray params=new JsonArray()
                            .add(jsonObject.getInteger("a_id", 1))
                            .add(jsonObject.getString("name", "lzheng"))
                            .add(jsonObject.getString("text", "lzheng"))
                            .add(str)
                            .add(1);
                    res.result().updateWithParams(addComment,params,updateResultAsyncResult -> {
                        if (updateResultAsyncResult.succeeded()){
                            res.result().close();
                            context.response().putHeader("content-type","application/json;charset=utf-8").end("{\"state\": 200,\"msg\": \"SUCCEED\"}");
                        }else{
                            res.result().close();
                            context.response().putHeader("content-type","application/json;charset=utf-8").end("{\"state\": 0,\"msg\": \"ERROR\"}");
                        }

                    });
                });

            });

        });

        //通过a_id获取博文
        router.get("/articleByAid/:a_id").handler(context->{
            int aID=Integer.parseInt(context.request().getParam("a_id"));
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res,context,h->{
                    JsonArray params = new JsonArray().add(aID);
                    res.result().queryWithParams(articleSqlByAid, params,result->{
                            List<JsonObject> jsonObject= result.result().getRows();
                            Map<String, Object> rtMap = reValue(200, "succeed", jsonObject.size(), jsonObject);
                            res.result().close();
                            context.response().putHeader("content-type", "application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });



        router.get("/type").handler(context->{
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    res.result().query("select * from myblog_type", result->{
                        List<JsonObject> jsonObject = result.result().getRows();
                        Map<String,Object> rtMap=reValue(200, "succeed", jsonObject.size(), jsonObject);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });

        router.get("/option").handler(context->{
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    res.result().query("select * from myblog_option where b_id=1", result->{
                        if (result.succeeded()) {
                            JsonObject jsonObject = result.result().getRows().get(0);
                            Map<String, Object> rtMap = reValue(200, "succeed", 1, jsonObject);
                            res.result().close();
                            context.response().putHeader("content-type", "application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                        }else{
                            Map<String, Object> rtMap = reValue(0, "没有进行设置", 1, null);
                            res.result().close();
                            context.response().putHeader("content-type", "application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                        }
                    });
                });
            });
        });



        router.get("/title/:t_id").handler(context->{
            int tID=Integer.parseInt(context.request().getParam("t_id"));
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    JsonArray params = new JsonArray().add(tID);
                    res.result().queryWithParams(titleSqlByTid, params,result->{
                        List<JsonObject> jsonObject = result.result().getRows();
                        Map<String,Object> rtMap=reValue(200, "succeed", jsonObject.size(), jsonObject);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });


        router.get("/comment/:a_id").handler(context->{
            int aID=Integer.parseInt(context.request().getParam("a_id"));
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    JsonArray params = new JsonArray().add(aID);
                    res.result().queryWithParams(commentSqlByAid, params,result->{
                        List<JsonObject> comment = result.result().getRows();
                        Map<String,Object> rtMap=reValue(200, "succeed", comment.size(), comment);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });


        router.mountSubRouter("/admin", new AdminVerticle(vertx).start());

        httpServer.requestHandler(router::accept);
        httpServer.listen(config().getInteger("http.port",8080));
    }



    public Map<String,Object> reValue(int state,String msg,int lentth,Object jsonObject){
        Map<String,Object> rtMap=new HashMap<>();
        rtMap.put("msg", "succeed");
        rtMap.put("data", jsonObject);
        rtMap.put("length", lentth);
        return rtMap;
    }

    public void hand(AsyncResult result, RoutingContext context, Consumer function){
        if (result.succeeded()){
            function.accept(null);
        }else {
            SQLConnection connection = (SQLConnection) result.result();
            connection.close();
            System.out.println(result.cause());
            context.response().putHeader("content-type","application/json;charset=utf-8").end("{\"state\": 0,\"msg\": \"数据库连接失败\"}");
        }
    }


    @Override
    public void stop() throws Exception {
        super.stop();
    }



}
