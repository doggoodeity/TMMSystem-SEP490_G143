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

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        // ✅ Ưu tiên public endpoint nếu chạy local
        String endpoint = isRunningOnRailway() ? privateEndpoint : publicEndpoint;

        System.out.println("👉 MinIO Endpoint in use: " + endpoint);
        System.out.println("👉 MinIO Access Key: " + accessKey);

        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)  // ✅ Dùng Access Key mới
                .build();
    }

    private boolean isRunningOnRailway() {
        return System.getenv("RAILWAY_ENVIRONMENT") != null;
    }

    @Bean
    public String minioPublicEndpoint() {
        return publicEndpoint;
    }
}
