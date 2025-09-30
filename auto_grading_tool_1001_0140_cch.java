// 代码生成时间: 2025-10-01 01:40:25
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

// 自动批改工具的服务接口
public interface AutoGradingService {
    void grade(String studentSolutionPath, String correctSolutionPath, Future<JsonObject> result);
}

// 自动批改工具服务实现
public class AutoGradingServiceImpl implements AutoGradingService {

    @Override
    public void grade(String studentSolutionPath, String correctSolutionPath, Future<JsonObject> result) {
        try {
            List<String> studentCode = Files.readAllLines(Paths.get(studentSolutionPath));
            List<String> correctCode = Files.readAllLines(Paths.get(correctSolutionPath));

            if (studentCode.size() != correctCode.size()) {
                // 如果代码行数不同，则自动评分为0
                result.complete(new JsonObject().put("grade", 0));
                return;
            }

            int grade = 0;
            for (int i = 0; i < studentCode.size(); i++) {
                if (studentCode.get(i).equals(correctCode.get(i))) {
                    grade++;
                }
            }

            // 根据行数计算得分
            result.complete(new JsonObject().put("grade", grade));

        } catch (Exception e) {
            // 错误处理
            result.fail(e);
        }
    }
}

// Verticle用于启动批改服务
public class AutoGradingVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) throws Exception {
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("auto.grading.service").register(AutoGradingService.class, new AutoGradingServiceImpl());
        startFuture.complete();
    }
}

// 批改工具的客户端类
public class AutoGradingClient {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(AutoGradingVerticle.class.getName(), res -> {
            if (res.succeeded()) {
                // 部署成功，进行批改
                vertx.eventBus().send("auto.grading.service", new JsonObject()
                        .put("studentSolutionPath", "/path/to/student/code")
                        .put("correctSolutionPath", "/path/to/correct/code"), reply -> {
                    if (reply.succeeded()) {
                        JsonObject result = (JsonObject) reply.result().body();
                        System.out.println("Grade: " + result.getInteger("grade"));
                    } else {
                        System.out.println("Failed to grade: " + reply.cause().getMessage());
                    }
                });
            } else {
                System.out.println("Deployment failed: " + res.cause().getMessage());
            }
        });
    }
}