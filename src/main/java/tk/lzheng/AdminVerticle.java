package tk.lzheng;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AdminVerticle extends AbstractVerticle {
    final static String loginSql="select * from user where u_name=? and u_password=?";
    Router router;
    public AdminVerticle(Router router) {
        this.router=router;
    }

    @Override
    public void start() throws Exception {
        router.get("/admin").handler(context -> {
            context.response().end("admin");
        });
        router.post("/login").handler(context->{
            String user = context.request().getParam("user");
            String password=MD5Utils.md5(context.request().getParam("password"));
            Mysqlclient.sqlClient.getConnection(result->{
                if (result.succeeded()){
                    JsonArray params = new JsonArray().add(user).add(password);
                    result.result().queryWithParams(loginSql,params,resultSetAsyncResult -> {
                        if (resultSetAsyncResult.result().getRows().size()>0){
                            context.session().put("login", true);
                        }else{
                            context.response().end("error");
                        }
                    });
                }else{
                    context.response().end("error");
                }
            });
        });



    }

}
