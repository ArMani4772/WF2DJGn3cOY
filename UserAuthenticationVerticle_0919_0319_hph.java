// 代码生成时间: 2025-09-19 03:19:00
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.AuthHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.JWTAuthHandler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.web.handler.SessionHandler;

import java.util.Map;

// 用户身份认证的Verticle类
public class UserAuthenticationVerticle extends AbstractVerticle {

    private JWTAuth jwtAuth;

    @Override
    public void start(Future<Void> startFuture) {
        // 配置JWT认证
        configureJWTAuth(res -> {
            if (res.succeeded()) {
                jwtAuth = res.result();
                // 创建Web路由并启动HTTP服务器
                createRouterAndStartServer(startFuture);
            } else {
                startFuture.fail(res.cause());
            }
        });
    }

    private void configureJWTAuth(Handler<AsyncResult<JWTAuth>> resultHandler) {
        // 配置JWT选项
        JWTOptions options = new JWTOptions().addPubSecKeys(
            new PubSecKeyOptions()
                .addPubKey("RS256",
                    "public_key.pem")
                .addSecKey("RS256",
                    "private_key.pem")
        );

        // 初始化JWT认证
        JWTAuth.create(vertx, options, resultHandler);
    }

    private void createRouterAndStartServer(Future<Void> startFuture) {
        // 创建Router对象
        Router router = Router.router(vertx);

        // 添加JWT认证处理器
        router.post("/login").handler(BodyHandler.create());
        router.post("/login").handler(this::authenticateUser);

        // 添加受保护的路由
        router.get("/protected").handler(this::requireUserAuthentication);

        // 启动HTTP服务器
        vertx.createHttpServer()
            .requestHandler(router::accept)
            .listen(config().getInteger("http.port", 8080), res -> {
                if (res.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(res.cause());
                }
            });
    }

    private void authenticateUser(RoutingContext context) {
        // 从请求体中获取用户凭证
        JsonObject authInfo = context.getBodyAsJson();
        String username = authInfo.getString("username");
        String password = authInfo.getString("password");

        // 验证用户凭证（这里使用假的验证逻辑，实际应用中应连接数据库验证）
        if ("admin".equals(username) && "password".equals(password)) {
            // 创建JWT令牌
            jwtAuth.generateToken(new JsonObject().put("userId", username), res -> {
                if (res.succeeded()) {
                    String token = res.result();
                    context.response()
                        .putHeader("content-type", "application/json")
                        .end(new JsonObject().put("token", token).encodePrettily());
                } else {
                    context.fail(401); // 认证失败
                }
            });
        } else {
            context.fail(401); // 认证失败
        }
    }

    private void requireUserAuthentication(RoutingContext context, AuthHandler authHandler) {
        // 添加JWT认证处理器
        authHandler.handle(context);
    }

    private JWTAuthHandler createJWTAuthHandler() {
        return jwtAuth
            .createHandler("RS256",
                new JWTAuthHandlerOptions()
                    .setJWTOptions(new JWTOptions().addPubSecKeys(
                        new PubSecKeyOptions()
                            .addPubKey("RS256",
                                    "public_key.pem"))));
    }
}
