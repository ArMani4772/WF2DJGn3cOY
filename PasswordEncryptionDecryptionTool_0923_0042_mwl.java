// 代码生成时间: 2025-09-23 00:42:23
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordEncryptionDecryptionTool extends AbstractVerticle {

    // 服务地址
    private static final String SERVICE_ADDRESS = "password.service";

    @Override
    public void start(Future<Void> startFuture) {
        ServiceBinder binder = new ServiceBinder(vertx);
        binder.setAddress(SERVICE_ADDRESS)
                .register(PasswordService.class, new PasswordServiceImpl());

        startFuture.complete();
    }

    // 密码服务接口
    public interface PasswordService {
        void encrypt(String plainText, Promise<JsonObject> result);
        void decrypt(String cipherText, Promise<JsonObject> result);
    }

    // 密码服务实现
    public static class PasswordServiceImpl implements PasswordService {

        // 随机生成密钥
        private static SecretKey generateKey() throws Exception {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128, new SecureRandom());
            return keyGenerator.generateKey();
        }

        @Override
        public void encrypt(String plainText, Promise<JsonObject> result) {
            try {
                SecretKey key = generateKey();
                byte[] keyBytes = key.getEncoded();
                byte[] plainTextBytes = plainText.getBytes();

                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"));
                byte[] cipherTextBytes = cipher.doFinal(plainTextBytes);
                String cipherText = Base64.getEncoder().encodeToString(cipherTextBytes);

                JsonObject response = new JsonObject().\put("cipherText", cipherText).put("key", Base64.getEncoder().encodeToString(keyBytes));
                result.complete(response);
            } catch (Exception e) {
                result.fail(e);
            }
        }

        @Override
        public void decrypt(String cipherText, Promise<JsonObject> result) {
            try {
                SecretKey key = new SecretKeySpec(Base64.getDecoder().decode(((JsonObject) vertx.sharedData().getLocalMap().get("key")).getString("key")), "AES");
                byte[] cipherTextBytes = Base64.getDecoder().decode(cipherText);

                Cipher cipher = Cipher.getInstance("AES");
                cipher.init(Cipher.DECRYPT_MODE, key);
                byte[] plainTextBytes = cipher.doFinal(cipherTextBytes);
                String plainText = new String(plainTextBytes);

                JsonObject response = new JsonObject().\put("plainText", plainText);
                result.complete(response);
            } catch (Exception e) {
                result.fail(e);
            }
        }
    }
}
