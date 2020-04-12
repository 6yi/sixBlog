package tk.lzheng;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminVerticle{
    final static String loginSql="SELECT * FROM myblog_user WHERE u_name=? AND u_password=?";
    final static String addArticle="INSERT I myblog_article (t_id,created,lasttime,title,`text`) VALUES(?,?,?,?,?)";
    final static String updateArticle="UPDATE myblog_article SET t_id=?,title=?,lastTime=?,`text`=?,viewable=? WHERE a_id=?";
    final static String updateTitle="UPDATE myblog_title SET t_id=?,title=?,`text`=?,viewable=? WHERE a_id=?";
    Vertx vertx;

    public AdminVerticle(Vertx vertx) {
        this.vertx=vertx;
    }


    public Router start() throws Exception {
        Router router = Router.router(vertx);

//        router.route("/*").handler(context->{
//            if (context.request().path().equals("/admin/login")){
//                context.next();
//            }else {
//                if (context.session().get("login") != null) {
//                    context.next();
//                } else {
//                    context.response().end("erro");
//                }
//            }
//        });


        router.post("/login").handler(context->{
            String user = context.request().getParam("user");
            String password=MD5Utils.md5(context.request().getParam("password"));
            System.out.println(password);
            Mysqlclient.sqlClient.getConnection(result->{
                if (result.succeeded()){
                    JsonArray params = new JsonArray().add(user).add(password);
                    result.result().queryWithParams(loginSql,params,resultSetAsyncResult -> {
                        List<JsonObject> jsonObjects = resultSetAsyncResult.result().getRows();
                        System.out.println("ok");
                        if (jsonObjects.size()>0){
                            context.session().put("login", true);
                            context.response().end("succeed");
                        }
                        result.result().close();
                        context.response().end("error");
                    });
                }else{
                    result.result().close();
                    context.response().end("error");
                }
            });
        });
        router.get("/hello").handler(context->{
            context.response().end("helo");
        });
        router.post("/updateArticle").handler(BodyHandler.create()).handler(context -> {
            //UPDATE myblog_article SET t_id=2,title=?,lastTime=?,`test`=?  WHERE a_id=?
            JsonObject bodyAsJson=context.getBodyAsJson();
            SimpleDateFormat sdf =new SimpleDateFormat("YYYY-MM-dd HH:MM:ss" );
            Date d= new Date();
            String str = sdf.format(d);
            JsonArray params = new JsonArray()
                    .add(bodyAsJson.getInteger("t_id", 1))
                    .add(bodyAsJson.getString("title", "java"))
                    .add(str)
                    .add(bodyAsJson.getString("text", "java"))
                    .add(bodyAsJson.getInteger("viewable", 1))
                    .add(bodyAsJson.getString("a_id", "9999"));
            //final static String updateTitle="UPDATE myblog_title SET t_id=2,title=?,text=? WHERE a_id=?";
            String text=bodyAsJson.getString("text", "java");
            int len=0;
            if (text.length()>20){
                len=20;
            }else{
                len=text.length();
            }
            //UPDATE myblog_title SET t_id=?,title=?,`text`=?,,viewable=? WHERE a_id=?"
            JsonArray params2 = new JsonArray()
                    .add(bodyAsJson.getInteger("t_id", 1))
                    .add(bodyAsJson.getString("title", "java"))
                    .add(text.substring(0, len))
                    .add(bodyAsJson.getInteger("viewable", 1))
                    .add(bodyAsJson.getString("a_id", "9999"));
            Mysqlclient.sqlClient.getConnection(sqlConnectionAsyncResult->{
                if (sqlConnectionAsyncResult.succeeded()){
                    SQLConnection sqlConnection = sqlConnectionAsyncResult.result();
                    LoggerUtils.logger_.info("ok");
                    sqlConnection.setAutoCommit(false, v->{
                        if(v.succeeded()){
                            LoggerUtils.logger_.info("ok2");
                            System.out.println(params+"\n"+params2);
                            sqlConnection.updateWithParams(updateArticle, params,up->{
                                LoggerUtils.logger_.info("ok3");
                                if (up.succeeded()){
                                    sqlConnection.updateWithParams(updateTitle, params2, up2->{
                                       if (up2.succeeded()){
                                           LoggerUtils.logger_.info("submit succeed");
                                           context.response().end("{'state':200,msg:'succeed'}");
                                       }else{
                                           sqlConnection.rollback(rb->{
                                               if (rb.succeeded()){
                                                   LoggerUtils.logger_.info("rb1 succeed");
                                               }
                                               context.response().end("{'state':0,msg:'error'}");
                                           });
                                       }
                                    });
                                }else{
                                    LoggerUtils.logger_.info("nk2");
                                    sqlConnection.rollback(rb->{
                                        if (rb.succeeded()){
                                            LoggerUtils.logger_.info("rb2 succeed");

                                        }
                                        context.response().end("{'state':0,msg:'error'}");
                                    });
                                }
                            });
                        }else{
                            sqlConnection.close();
                            context.response().end("erro");
                        }
                    });
                }else {
                    sqlConnectionAsyncResult.result().close();
                    context.response().end("error");
                }
            });
        });




        return router;
    }

}
