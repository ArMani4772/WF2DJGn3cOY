// 代码生成时间: 2025-09-18 19:12:31
 * This program is designed to be clear, maintainable, and extensible.
 * It includes proper error handling and documentation.
 */

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.CorsHandler;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class TestReportGenerator extends AbstractVerticle {

    // Entry point of the application
    @Override
    public void start(Future<Void> startFuture) {
        // Define the router
        Router router = Router.router(vertx);

        // Enable CORS for all routes
        router.route().handler(CorsHandler.allowAll());

        // Handle errors
        router.errorHandler(500, new ErrorHandler());

        // Serve static files from the 'public' directory
        router.route("/*").handler(StaticHandler.create("public"));

        // Handle JSON body data
        router.route().handler(BodyHandler.create());

        // Define the route for generating the test report
        router.post("/report").handler(this::handleReportGeneration);

        // Start the HTTP server and listen on port 8080
        vertx.createHttpServer()
            .requestHandler(router)
            .listen(8080, http -> {
                if (http.succeeded()) {
                    System.out.println("HTTP server started on port 8080");
                    startFuture.complete();
                } else {
                    startFuture.fail(http.cause());
                }
            });
    }

    // Handle report generation
    private void handleReportGeneration(RoutingContext context) {
        // Extract the JSON data from the request body
        JsonObject requestBody = context.getBodyAsJson();

        // Validate the request data
        if (requestBody == null || requestBody.isEmpty()) {
            context.response().setStatusCode(400).end("Invalid request data");
            return;
        }

        // Extract the test results from the JSON data
        JsonArray testResults = requestBody.getJsonArray("testResults");
        if (testResults == null || testResults.isEmpty()) {
            context.response().setStatusCode(400).end("Test results are missing");
            return;
        }

        // Generate the test report
        try {
            String reportContent = generateReportContent(testResults);
            String reportFileName = "test_report_" + System.currentTimeMillis() + ".txt";
            writeReportToFile(reportFileName, reportContent);

            // Send the report file name back to the client
            context.response().setStatusCode(200).end(reportFileName);
        } catch (IOException e) {
            // Handle any errors during report generation
            context.response().setStatusCode(500).end("Error generating the test report");
        }
    }

    // Generate the content of the test report
    private String generateReportContent(JsonArray testResults) {
        StringBuilder reportContent = new StringBuilder("Test Report

");
        for (int i = 0; i < testResults.size(); i++) {
            JsonObject testResult = testResults.getJsonObject(i);
            String testName = testResult.getString("testName");
            String testStatus = testResult.getString("testStatus");
            String testMessage = testResult.getString("testMessage");

            reportContent.append("Test Name: ").append(testName).append("
");
            reportContent.append("Test Status: ").append(testStatus).append("
");
            reportContent.append("Test Message: ").append(testMessage).append("

");
        }

        return reportContent.toString();
    }

    // Write the report content to a file
    private void writeReportToFile(String fileName, String content) throws IOException {
        File reportFile = new File("public/reports/" + fileName);
        try (FileWriter writer = new FileWriter(reportFile)) {
            writer.write(content);
        }
    }
}
