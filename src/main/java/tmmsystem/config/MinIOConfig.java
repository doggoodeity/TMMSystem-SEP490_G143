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
        return MinioClient.builder()
                .endpoint(privateEndpoint)
                .credentials(rootUser, rootPassword)
                .build();
    }
    
    @Bean
    public String minioPublicEndpoint() {
        return publicEndpoint;
    }
}
