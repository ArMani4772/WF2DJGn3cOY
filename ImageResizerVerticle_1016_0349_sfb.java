// 代码生成时间: 2025-10-16 03:49:25
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.apache.commons.io.FileUtils;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;
import org.apache.commons.io.FilenameUtils;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

// ImageResizerVerticle 是一个 Vert.x 事件处理器，用于处理图片尺寸批量调整任务
public class ImageResizerVerticle extends AbstractVerticle {

    // 定义服务代理构建器，用于创建图片处理服务的代理实例
    private ServiceProxyBuilder proxyBuilder = new ServiceProxyBuilder(vertx);

    // 定义图片处理器服务的地址
    private static final String IMAGE_PROCESSOR_SERVICE_ADDRESS = "image.processor.service";

    // 在启动时初始化服务代理
    @Override
    public void start(Future<Void> startFuture) {
        super.start(startFuture);

        // 初始化图片处理器服务的代理
        proxyBuilder.build(IImageProcessor.class, IMAGE_PROCESSOR_SERVICE_ADDRESS);

        // 注册图片尺寸调整事件处理器
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("resize.images", message -> {
            JsonObject request = (JsonObject) message.body();
            String sourceDir = request.getString("sourceDir");
            String targetDir = request.getString("targetDir");
            int width = request.getInteger("width");
            int height = request.getInteger("height");

            try {
                resizeImages(sourceDir, targetDir, width, height);
                message.reply(new JsonObject().put("status", "success"));
            } catch (IOException e) {
                message.reply(new JsonObject().put("status", "failure").put("message", e.getMessage()));
            }
        });
    }

    // 图片尺寸调整方法
    private void resizeImages(String sourceDir, String targetDir, int width, int height) throws IOException {
        // 确保目标目录存在
        File targetDirFile = new File(targetDir);
        if (!targetDirFile.exists() && !targetDirFile.mkdirs()) {
            throw new IOException("Failed to create target directory");
        }

        // 获取源目录中的所有图片文件
        File[] imageFiles = new File(sourceDir).listFiles((dir, name) -> FilenameUtils.isImageFile(name));
        if (imageFiles == null || imageFiles.length == 0) {
            throw new IOException("No image files found in the source directory");
        }

        // 调整每个图片文件的尺寸
        for (File imageFile : imageFiles) {
            // 读取图片
            BufferedImage originalImage = ImageIO.read(imageFile);
            if (originalImage == null) {
                throw new IOException("Failed to read image: " + imageFile.getAbsolutePath());
            }

            // 调整图片尺寸
            BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
            resizedImage.getGraphics().drawImage(originalImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0, null);

            // 保存调整后的图片到目标目录
            File targetFile = new File(targetDirFile, UUID.randomUUID().toString() + FilenameUtils.getExtension(imageFile.getName()));
            ImageIO.write(resizedImage, FilenameUtils.getExtension(imageFile.getName()), targetFile);
        }
    }
}
