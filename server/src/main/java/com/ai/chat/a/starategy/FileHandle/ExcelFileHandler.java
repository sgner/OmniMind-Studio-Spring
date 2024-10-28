package com.ai.chat.a.starategy.FileHandle;
import com.ai.chat.a.starategy.FileHandlerStrategy;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public class ExcelFileHandler implements FileHandlerStrategy {
    @Override
    public String handleFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        if (fileName.endsWith(".xlsx")) {
            return handleXlsxFile(file);
        } else if (fileName.endsWith(".xls")) {
            return handleXlsFile(file);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: 只支持 .xls 和 .xlsx 文件");
        }
    }

    private String handleXlsxFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            return extractExcelContent(workbook);
        }
    }

    private String handleXlsFile(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new HSSFWorkbook(inputStream)) {
            return extractExcelContent(workbook);
        }
    }

    private String extractExcelContent(Workbook workbook) {
        StringBuilder content = new StringBuilder();
        for (Sheet sheet : workbook) {
            for (Row row : sheet) {
                for (Cell cell : row) {
                    content.append(cell.toString()).append("\t");
                }
                content.append("\n");
            }
        }
        return content.toString();
    }
}
