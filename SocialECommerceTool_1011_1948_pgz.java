// 代码生成时间: 2025-10-11 19:48:50
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.JWTAuth;

// 定义服务接口
public interface SocialECommerceService {
    String CREATE_POST = "socialECommerce.createPost";
    void createPost(String content, Handler<AsyncResult<JsonObject>> resultHandler);
}

// 实现服务接口
public class SocialECommerceServiceImpl implements SocialECommerceService {
    @Override
    public void createPost(String content, Handler<AsyncResult<JsonObject>> resultHandler) {
        // 模拟数据库存储逻辑
        JsonObject post = new JsonObject().put("content", content);
        resultHandler.handle(Future.succeededFuture(post));
    }
}

// Verticle 类，负责初始化和启动服务
public class SocialECommerceTool extends AbstractVerticle {
    private JWTAuth authProvider;
    private ServiceBinder serviceBinder;
    private Router router;

    @Override
    public void start(Future<Void> startFuture) {
        // 初始化 JWT 认证
        authProvider = JWTAuth.create(vertx);

        // 初始化服务代理
        serviceBinder = new ServiceBinder(vertx);
        serviceBinder.setAddress("socialECommerce").register(SocialECommerceService.class, new SocialECommerceServiceImpl());

        // 初始化路由
        router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.post("/posts").handler(this::handleCreatePost);

        // 启动 HTTP 服务器
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(config().getInteger("http.port", 8080), ar -> {
                if (ar.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(ar.cause());
                }
            });
    }

    private void handleCreatePost(RoutingContext context) {
        String content = context.getBodyAsString();
        JsonObject authHeader = context.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            context.fail(401);
            return;
        }

        String token = authHeader.substring(7);
        authProvider.authenticate(new JsonObject().put("jwt", token), ar -> {
            if (ar.succeeded()) {
                // 认证成功，创建帖子
                User user = ar.result();
                serviceBinder.getProxy(SocialECommerceService.class, res -> {
                    if (res.succeeded()) {
                        SocialECommerceService service = res.result();
                        service.createPost(content, createPostRes -> {
                            if (createPostRes.succeeded()) {
                                context.response().setStatusCode(201).end(createPostRes.result().encode());
                            } else {
                                context.fail(500);
                            }
                        });
                    } else {
                        context.fail(500);
                    }
                });
            } else {
                context.fail(401);
            }
        });
    }
}
