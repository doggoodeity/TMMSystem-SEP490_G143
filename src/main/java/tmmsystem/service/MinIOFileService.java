package tmmsystem.service;

import io.minio.*;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class MinIOFileService {
    
    private final MinioClient minioClient;
    private final String mainBucket;
    private final String publicEndpoint;
    
    public MinIOFileService(MinioClient minioClient,
                           @Value("${minio.bucket.main}") String mainBucket,
                           @Value("${minio.public.endpoint}") String publicEndpoint) {
        this.minioClient = minioClient;
        this.mainBucket = mainBucket;
        this.publicEndpoint = publicEndpoint;
    }
    
    @jakarta.annotation.PostConstruct
    public void initializeBuckets() {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(mainBucket).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(mainBucket).build());
                log.info("Created bucket: {}", mainBucket);
            }
        } catch (Exception e) {
            log.error("Error initializing MinIO bucket", e);
        }
    }
    
    // Contract file operations
    public String uploadContractFile(MultipartFile file, Long contractId) throws Exception {
        validateFile(file);
        String fileName = generateContractFileName(contractId, file.getOriginalFilename());
        String objectName = "contracts/" + contractId + "/" + fileName;
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(mainBucket)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        
        log.info("Uploaded contract file: {} for contract ID: {}", objectName, contractId);
        return objectName;
    }
    
    public String getContractFileUrl(Long contractId) {
        try {
            String objectName = "contracts/" + contractId + "/";
            // List objects to find the actual file
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(mainBucket)
                    .prefix(objectName)
                    .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    return publicEndpoint + "/" + mainBucket + "/" + item.objectName();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting contract file URL for contract ID: {}", contractId, e);
            return null;
        }
    }
    
    public InputStream downloadContractFile(Long contractId) throws Exception {
        String objectName = "contracts/" + contractId + "/";
        // Find the actual file
        Iterable<Result<Item>> results = minioClient.listObjects(
            ListObjectsArgs.builder()
                .bucket(mainBucket)
                .prefix(objectName)
                .build()
        );
        
        for (Result<Item> result : results) {
            Item item = result.get();
            if (!item.isDir()) {
                return minioClient.getObject(
                    GetObjectArgs.builder()
                        .bucket(mainBucket)
                        .object(item.objectName())
                        .build()
                );
            }
        }
        throw new Exception("Contract file not found for ID: " + contractId);
    }
    
    public void deleteContractFile(Long contractId) {
        try {
            String objectName = "contracts/" + contractId + "/";
            List<DeleteObject> objectsToDelete = StreamSupport.stream(
                minioClient.listObjects(
                    ListObjectsArgs.builder()
                        .bucket(mainBucket)
                        .prefix(objectName)
                        .build()
                ).spliterator(), false)
                .map(result -> {
                    try {
                        return result.get().objectName();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(DeleteObject::new)
                .collect(Collectors.toList());
            
            if (!objectsToDelete.isEmpty()) {
                minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                        .bucket(mainBucket)
                        .objects(objectsToDelete)
                        .build()
                );
                log.info("Deleted contract files for contract ID: {}", contractId);
            }
        } catch (Exception e) {
            log.error("Error deleting contract files for contract ID: {}", contractId, e);
        }
    }
    
    // Production Order file operations (using same bucket with folder structure)
    public String uploadProductionOrderFile(MultipartFile file, Long productionOrderId) throws Exception {
        validateFile(file);
        String fileName = generateProductionOrderFileName(productionOrderId, file.getOriginalFilename());
        String objectName = "production-orders/" + productionOrderId + "/" + fileName;
        
        minioClient.putObject(
            PutObjectArgs.builder()
                .bucket(mainBucket)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
        
        log.info("Uploaded production order file: {} for PO ID: {}", objectName, productionOrderId);
        return objectName;
    }
    
    public String getProductionOrderFileUrl(Long productionOrderId) {
        try {
            String objectName = "production-orders/" + productionOrderId + "/";
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(mainBucket)
                    .prefix(objectName)
                    .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    return publicEndpoint + "/" + mainBucket + "/" + item.objectName();
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting production order file URL for PO ID: {}", productionOrderId, e);
            return null;
        }
    }
    
    // File validation
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/pdf") && 
             !contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Only PDF and image files are allowed");
        }
    }
    
    // File name generation
    private String generateContractFileName(Long contractId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return "contract_" + contractId + "_" + System.currentTimeMillis() + extension;
    }
    
    private String generateProductionOrderFileName(Long productionOrderId, String originalFilename) {
        String extension = getFileExtension(originalFilename);
        return "production_order_" + productionOrderId + "_" + System.currentTimeMillis() + extension;
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
