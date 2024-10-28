package com.ai.chat.a.starategy.FileHandle;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        return extractFileNames(file);
    }
    private String extractFileNames(MultipartFile file) throws IOException {
        StringBuilder result = new StringBuilder();

        // 使用 ZipInputStream 遍历 zip 文件中的每个条目
        try (ZipInputStream zis = new ZipInputStream(file.getInputStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName(); // 获取文件名（包含层级信息）

                // 按照层级结构输出
                result.append(fileName).append("\n");
            }
        }

        return result.toString();
    }

    //tar
    private String extractTarFileNames(InputStream inputStream) throws IOException {
        StringBuilder result = new StringBuilder();
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(inputStream)) {
            TarArchiveEntry entry;
            while ((entry = tarIn.getNextTarEntry()) != null) {
                result.append(entry.getName()).append("\n");
            }
        }
        return result.toString();
    }
    //tar.gz
    private String extractTarGzFileNames(InputStream inputStream) throws IOException {
        try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream)) {
            return extractTarFileNames(gzipIn);
        }
    }

    //7z
   private String sevenZipFileNames(MultipartFile file) throws IOException {
       File tempFile = new File(System.getProperty("java.io.tmpdir"), file.getOriginalFilename());
       file.transferTo(tempFile);

       StringBuilder result = new StringBuilder();

       try (RandomAccessFile randomAccessFile = new RandomAccessFile(tempFile, "r")) {
           IInArchive archive = SevenZip.openInArchive(ArchiveFormat.SEVEN_ZIP, new RandomAccessFileInStream(randomAccessFile));
           int numberOfItems = archive.getNumberOfItems();

           for (int i = 0; i < numberOfItems; i++) {
               String itemPath = archive.getStringProperty(i, net.sf.sevenzipjbinding.PropID.PATH);
               result.append(itemPath).append("\n");
           }

           archive.close();
       }

       return result.toString();
   }
//    private String extractSingleFileName(InputStream inputStream, String type) throws IOException {
//        if (type.equals("gzip")) {
//            try (GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(inputStream)) {
//                return "gzip-file";
//            }
//        } else if (type.equals("bzip2")) {
//            try (BZip2CompressorInputStream bzipIn = new BZip2CompressorInputStream(inputStream)) {
//                return "bzip2-file";
//            }
//        }
//        return null;
//    }

}
