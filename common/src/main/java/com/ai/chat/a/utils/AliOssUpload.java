package com.ai.chat.a.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.ai.chat.a.enums.ErrorCode;
import com.ai.chat.a.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * 阿里云上传文件接口
 *
 * @author pepedd864
 * @since 2024/5/31
 */
@Component
@Slf4j
public class AliOssUpload {
  @Value("${alioss.project-name}")
  private String projectName;
  @Value("${alioss.endpoint}")
  private String endpoint;
  @Value("${alioss.access-key-id}")
  private String accessKeyId;
  @Value("${alioss.access-key-secret}")
  private String accessKeySecret;
  @Value("${alioss.bucket-name}")
  private String bucketName;
//  @Override
//  public String[] upload(MultipartFile... file) {
//    // 初始化 urls 数组以存储每个文件的 URL
//    String[] urls = new String[file.length];
//    for (int i = 0; i < file.length; i++) {
//      MultipartFile f = file[i];
//      try {
//        InputStream inputStream = f.getInputStream();
//        String filename = f.getOriginalFilename();
//        // 取后缀名
//        String suffix = filename.substring(filename.lastIndexOf("."));
//        // 使用 时间 生成文件名
//        String newFileName = System.currentTimeMillis() + suffix;
//        String filePath = projectName + "/" + newFileName;
//        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, filePath, inputStream);
//        oss.putObject(putObjectRequest);
//        // 将生成的 URL 存储在 urls 数组中
//        urls[i] = domain + "/" + projectName + "/" + newFileName;
//      } catch (Exception e) {
//        throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传文件失败" + e.getMessage());
//      }
//    }
//    // 返回 urls 数组
//    return urls;
//  }
  public String[] upload(MultipartFile... file) {
    // 创建OSSClient实例。
    OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    String[] urls = new String[file.length];
    for(int i = 0;i<file.length;i++){
      MultipartFile f = file[i];
      String fileName = f.getOriginalFilename();
      String suffix = fileName.substring(fileName.lastIndexOf("."));
      String objectName = UUID.randomUUID()+suffix;
      String filePath = projectName + "/" + objectName;
      try {
        // 创建PutObject请求。
        ossClient.putObject(bucketName, filePath, new ByteArrayInputStream(f.getBytes()));
      } catch (OSSException oe) {
        System.out.println("Caught an OSSException, which means your request made it to OSS, "
                + "but was rejected with an error response for some reason.");
        System.out.println("Error Message:" + oe.getErrorMessage());
        System.out.println("Error Code:" + oe.getErrorCode());
        System.out.println("Request ID:" + oe.getRequestId());
        System.out.println("Host ID:" + oe.getHostId());
      } catch (ClientException ce) {
        System.out.println("Caught an ClientException, which means the client encountered "
                + "a serious internal problem while trying to communicate with OSS, "
                + "such as not being able to access the network.");
        System.out.println("Error Message:" + ce.getMessage());
      } catch (IOException e) {
          throw new RuntimeException(e);
      }
      //文件访问路径规则 https://BucketName.Endpoint/ObjectName
      StringBuilder stringBuilder = new StringBuilder("https://");
      stringBuilder
              .append(bucketName)
              .append(".")
              .append(endpoint)
              .append("/")
              .append(projectName)
              .append("/")
              .append(objectName);

      log.info("文件上传到:{}", stringBuilder.toString());
      urls[i] = stringBuilder.toString();
    }

    ossClient.shutdown();
    return urls;
  }

  /**
   * 删除文件
   *
   * @param fileName
   * @return
   */

  public ArrayList<String> delete(String... fileName) {
      OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
      ArrayList<String> failedFIle = new ArrayList<>();
      for (String f : fileName) {
        try {
            ossClient.deleteObject(bucketName, f);
            log.info("删除文件:{}", f);
        } catch (Exception e) {
            failedFIle.add(f);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getMessage()+" 删除文件失败" + e.getMessage());
        }
    }
    ossClient.shutdown();
    return failedFIle;
  }
}
