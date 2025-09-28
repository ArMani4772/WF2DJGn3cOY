// 代码生成时间: 2025-09-29 03:59:20
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * Content Distribution Network using Vert.x
 * This program sets up a simple CDN using Vert.x framework.
 * It serves static files and allows CORS for external requests.
 */
public class ContentDistributionNetwork extends AbstractVerticle {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);

        // Enable CORS for external requests
        router.route().handler(CorsHandler.create("*").allowedMethod(io.vertx.core.http.HttpMethod.GET));

        // Handling Body for POST requests
        router.route().handler(BodyHandler.create());

        // Serving static files from the public directory
        router.route("/*").handler(StaticHandler.create("public"));

        vertx.createHttpServer()
            .requestHandler(router)
            .listen(PORT, ar -> {
                if (ar.succeeded()) {
                    System.out.println("Content Distribution Network is running on http://" + HOST + ":" + PORT);
                    startFuture.complete();
                } else {
                    startFuture.fail(ar.cause());
                }
            });
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        vertx.close(stopFuture);
    }

    // Main method to start the Verticle
    public static void main(String[] args) {
        var vertx = io.vertx.core.Vertx.vertx();
        var deployment = vertx.deployVerticle(new ContentDistributionNetwork());
        deployment.onComplete(res -> {
            if (res.succeeded()) {
                System.out.println("Content Distribution Network deployed successfully.");
            } else {
                System.out.println("Failed to deploy Content Distribution Network.");
                res.cause().printStackTrace();
            }
        });
    }
}
