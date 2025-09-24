// 代码生成时间: 2025-09-24 20:09:32
// 使用 Vert.x 框架创建的 HTTP 请求处理器
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class HttpHandlerExample extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) {
        // 创建 HTTP 服务器
        HttpServer server = vertx.createHttpServer();

        // 创建 Router 来处理路由
        Router router = Router.router(vertx);

        // 静态文件处理
        router.route("/static/*").handler(StaticHandler.create());

        // 处理 JSON 数据的处理器
        router.route().handler(BodyHandler.create().setUploadsDirectory("uploads"));

        // 简单的 HTTP GET 请求处理器
        router.route(HttpMethod.GET, "/").handler(this::handleRequest);

        // SockJS 和 EventBus 桥接处理器
        BridgeOptions options = new BridgeOptions();
        options.addOutboundPermitted(new PermittedOptions().setAddress("news.address"));
        SockJSHandlerOptions sockJSHandlerOptions = new SockJSHandlerOptions().setHeartbeatInterval(1000).setOptions(options);
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, sockJSHandlerOptions);
        router.route("/eventbus/*").handler(sockJSHandler);

        // 启动 HTTP 服务器并监听路由
        server.requestHandler(router::accept).listen(config().getInteger("http.port", 8080), result -> {
            if (result.succeeded()) {
                startFuture.complete();
                System.out.println("HTTP server started on port 8080");
            } else {
                startFuture.fail(result.cause());
            }
        });
    }

    private void handleRequest(RoutingContext context) {
        HttpServerResponse response = context.response();
        response.putHeader("content-type", "text/plain");
        response.end("Hello from Vert.x!");
    }
}
