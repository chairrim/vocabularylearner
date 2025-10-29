package com.example.vocabularylearner.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.vocabularylearner.entity.Word;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExcelUtils {
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    /**
     * 检查存储权限
     */
    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return true; // Android 13+ 不需要读取权限
        }
        
        int permission = ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE");
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 请求存储权限
     */
    public static void requestStoragePermission(Context context, int requestCode) {
        ActivityCompat.requestPermissions(
                (android.app.Activity) context,
                PERMISSIONS_STORAGE,
                requestCode
        );
    }

    /**
     * 解析Excel文件
     */
    public static List<Word> parseExcelFile(Context context, Uri uri) throws IOException {
        List<Word> words = new ArrayList<>();
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        
        if (inputStream == null) {
            throw new IOException("无法打开文件");
        }

        Workbook workbook = null;
        try {
            // 根据文件扩展名判断是xls还是xlsx
            String fileName = context.getContentResolver().getType(uri);
            if (fileName != null && fileName.contains("vnd.openxmlformats-officedocument.spreadsheetml.sheet")) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                workbook = new HSSFWorkbook(inputStream);
            }

            // 读取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return words;
            }

            // 迭代行（跳过标题行）
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) {
                rowIterator.next(); // 跳过标题行
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // 获取单元格数据
                Cell englishCell = row.getCell(0);
                Cell phoneticCell = row.getCell(1);
                Cell chineseCell = row.getCell(2);
                Cell exampleCell = row.getCell(3);

                if (englishCell == null) {
                    continue; // 跳过空行
                }

                String english = getCellValue(englishCell).trim();
                String phonetic = phoneticCell != null ? getCellValue(phoneticCell).trim() : "";
                String chinese = chineseCell != null ? getCellValue(chineseCell).trim() : "";
                String example = exampleCell != null ? getCellValue(exampleCell).trim() : "";

                if (!english.isEmpty()) {
                    words.add(new Word(english, phonetic, chinese, example));
                }
            }
        } finally {
            if (workbook != null) {
                workbook.close();
            }
            inputStream.close();
        }

        return words;
    }

    /**
     * 获取单元格的值
     */
    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}
