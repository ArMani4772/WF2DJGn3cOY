// 代码生成时间: 2025-10-14 03:32:24
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DigitalSignatureTool extends AbstractVerticle {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public DigitalSignatureTool() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048, SecureRandom.getInstanceStrong());

            var keyPair = keyPairGenerator.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // Bind the service to the event bus with a unique address
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress("digital.signature.tool").register(DigitalSignatureService.class, new DigitalSignatureServiceImpl());
        startPromise.complete();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }
}

class DigitalSignatureServiceImpl implements DigitalSignatureService {

    private final DigitalSignatureTool digitalSignatureTool;

    public DigitalSignatureServiceImpl(DigitalSignatureTool digitalSignatureTool) {
        this.digitalSignatureTool = digitalSignatureTool;
    }

    @Override
    public void sign(String message, Handler<AsyncResult<JsonObject>> resultHandler) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(digitalSignatureTool.getPrivateKey());
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signedMessage = signature.sign();
            String signatureBase64 = Base64.getEncoder().encodeToString(signedMessage);
            JsonObject response = new JsonObject().put("originalMessage", message).put("signature", signatureBase64);
            resultHandler.handle(Future.succeededFuture(response));
        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject().put("error", e.getMessage());
            resultHandler.handle(Future.failedFuture(e));
        }
    }
}

interface DigitalSignatureService {
    String SIGN_ADDRESS = "digital.signature.sign";

    void sign(String message, Handler<AsyncResult<JsonObject>> resultHandler);
}

public class Main {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new DigitalSignatureTool(), res -> {
            if (res.succeeded()) {
                System.out.println("Digital Signature Tool is deployed successfully.");
            } else {
                System.out.println("Failed to deploy Digital Signature Tool.");
                res.cause().printStackTrace();
            }
        });
    }
}
