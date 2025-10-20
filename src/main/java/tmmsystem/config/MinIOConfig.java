package tmmsystem.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinIOConfig {

    @Value("${minio.public.endpoint}")
    private String publicEndpoint;

    @Value("${minio.private.endpoint}")
    private String privateEndpoint;

    @Value("${minio.root.user}")
    private String rootUser;

    @Value("${minio.root.password}")
    private String rootPassword;

    @Bean
    public MinioClient minioClient() {
        // ✅ Ưu tiên public endpoint nếu đang chạy local
        String endpoint = isRunningOnRailway() ? privateEndpoint : publicEndpoint;

        System.out.println("👉 MinIO Endpoint in use: " + endpoint);
        System.out.println("👉 MinIO User: " + rootUser);

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(rootUser, rootPassword)
                .build();
    }

    private boolean isRunningOnRailway() {
        // Railway luôn inject biến này cho tất cả container
        return System.getenv("RAILWAY_ENVIRONMENT") != null;
    }

    @Bean
    public String minioPublicEndpoint() {
        return publicEndpoint;
    }
}
