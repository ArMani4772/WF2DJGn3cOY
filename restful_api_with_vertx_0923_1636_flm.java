// 代码生成时间: 2025-09-23 16:36:19
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.List;

// 定义接口，用于定义RESTful API的行为
interface MyService {
    String sayHello(String name);
}

// 实现接口的具体服务
class MyServiceImpl implements MyService {
    @Override
    public String sayHello(String name) {
        return "Hello, " + name + "!";
    }
}

// 定义Verticle，Vert.x中的组件，用于启动服务器和路由
public class RestfulApiWithVertx extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        // 创建一个Router对象，用于定义HTTP路由
        Router router = Router.router(vertx);

        // 通过ServiceProxyBuilder创建服务代理，通过代理可以远程调用服务
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx);
        MyService myService = builder.setAddress("myServiceAddress").build(MyService.class);

        // 设置静态文件服务，用于提供前端资源
        router.route("/").handler(StaticHandler.create());

        // 定义RESTful API路由
        router.post("/api/hello").handler(ctx -> {
            myService.sayHello(ctx.request().body().toString()), res -> {
                if (res.succeeded()) {
                    ctx.response().setStatusCode(200).end(res.result());
                } else {
                    ctx.response().setStatusCode(500).end("Server Error");
                }
            });
        });

        // 添加错误处理
        router.errorHandler(500, ctx -> {
            ctx.response().setStatusCode(500).end("Internal Server Error");
        });

        // 创建HTTP服务器监听8080端口
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, result -> {
                if (result.succeeded()) {
                    startPromise.complete();
                    System.out.println("Server started on port 8080");
                } else {
                    startPromise.fail(result.cause());
                }
            });
    }
}
