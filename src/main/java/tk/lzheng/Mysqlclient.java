package tk.lzheng;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;

public class Mysqlclient {
    static AsyncSQLClient sqlClient;
    public Mysqlclient(Vertx vertx,JsonObject conf) {
        JsonObject mySQLClientConfig = new JsonObject()
                .put("host", conf.getString("mysql.host","localhost"))
                .put("port", conf.getInteger("mysql.port",3306))
                .put("username", conf.getString("mysql.username","root"))
                .put("password", conf.getString("mysql.password","root"))
                .put("database", conf.getString("mysql.database","myblog"));
        sqlClient = MySQLClient.createShared(vertx, mySQLClientConfig);
    }
}
