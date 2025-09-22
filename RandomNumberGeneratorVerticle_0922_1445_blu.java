// 代码生成时间: 2025-09-22 14:45:34
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
# FIXME: 处理边界情况
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNumberGeneratorVerticle extends AbstractVerticle {

    private static final String ENDPOINT = "/random";
    private static final int DEFAULT_MIN_VALUE = 1;
    private static final int DEFAULT_MAX_VALUE = 100;
    private Random random = new Random();

    @Override
    public void start(Future<Void> startFuture) throws Exception {
# 增强安全性
        // 创建HTTP服务器并配置路由
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);

        // 设置Body处理器
        router.route().handler(BodyHandler.create());

        // 添加路由以处理请求
        router.post(ENDPOINT).handler(this::handleRandomNumberRequest);

        // 启动服务器
        server.requestHandler(router)
            .listen(config().getInteger("http.port", 8080), result -> {
                if (result.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(result.cause());
                }
            });
    }

    private void handleRandomNumberRequest(RoutingContext context) {
        HttpServerRequest request = context.request();
        // 从请求中提取参数
        int minValue = request.getParam("min") != null ? Integer.parseInt(request.getParam("min")) : DEFAULT_MIN_VALUE;
# NOTE: 重要实现细节
        int maxValue = request.getParam("max") != null ? Integer.parseInt(request.getParam("max")) : DEFAULT_MAX_VALUE;

        // 检查参数值
        if (minValue > maxValue) {
            context.response().setStatusCode(400).end("Invalid range: min value cannot be greater than max value");
# TODO: 优化性能
            return;
        }

        // 生成随机数
        int randomNumber = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);

        // 返回响应
        context.response()
            .putHeader("content-type", "application/json")
            .end("{"randomNumber": " + randomNumber + "}");
    }
}
