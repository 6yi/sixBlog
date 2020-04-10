package tk.lzheng;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


public class BlogVerticle extends AbstractVerticle {
    static final String articleSqlByAid="select * from myblog_article where a_id=?";
    static final String titleSqlByTid="select * from myblog_title where t_id=?";
    static final String commentSqlByAid="select * from myblog_comment where a_id=?";

    AsyncSQLClient sqlClient;
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        HttpServer httpServer = vertx.createHttpServer();
        Router router = Router.router(vertx);
        new Mysqlclient(vertx, config());

        //获取全部标题
        router.get("/title").handler(context->{
                Mysqlclient.sqlClient.getConnection(res->{
                    hand(res, context, h->{
                        res.result().query("select * from myblog_title",result->{
                            List<JsonObject> jsonObjects = result.result().getRows();
                            Map<String,Object> rtMap=new HashMap<>();
                            rtMap.put("state", 200);
                            rtMap.put("msg", "succeed");
                            rtMap.put("length", jsonObjects.size());
                            rtMap.put("titles", jsonObjects);
                            res.result().close();
                            context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
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
                        JsonObject jsonObject = result.result().getRows().get(0);
                        Map<String,Object> rtMap=new HashMap<>();
                        rtMap.put("msg", "succeed");
                        rtMap.put("article", jsonObject);
                        rtMap.put("state", 200);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });



        router.get("/type").handler(context->{
            Mysqlclient.sqlClient.getConnection(res->{
                hand(res, context, h->{
                    res.result().query("select * from myblog_type", result->{
                        List<JsonObject> jsonObject = result.result().getRows();
                        Map<String,Object> rtMap=new HashMap<>();
                        rtMap.put("state", 200);
                        rtMap.put("msg", "succeed");
                        rtMap.put("types", jsonObject);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
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
                        Map<String,Object> rtMap=new HashMap<>();
                        rtMap.put("state", 200);
                        rtMap.put("msg", "succeed");
                        rtMap.put("titles", jsonObject);
                        rtMap.put("length", jsonObject.size());
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
                        Map<String,Object> rtMap=new HashMap<>();
                        rtMap.put("msg", "succeed");
                        rtMap.put("comments",comment);
                        rtMap.put("state", 200);
                        res.result().close();
                        context.response().putHeader("content-type","application/json;charset=utf-8").end(new JsonObject(rtMap).toBuffer());
                    });
                });
            });
        });



        httpServer.requestHandler(router::accept);
        httpServer.listen(config().getInteger("http.port",8080));
    }


    public void hand(AsyncResult result, RoutingContext context, Consumer function){
        if (result.succeeded()){
            function.accept(null);
        }else {
            System.out.println(result.cause());
            context.response().putHeader("content-type","application/json;charset=utf-8").end("{\"state\": 0,\"msg\": \"数据库连接失败\"}");
        }
    }




    @Override
    public void stop() throws Exception {
        super.stop();
    }



}
