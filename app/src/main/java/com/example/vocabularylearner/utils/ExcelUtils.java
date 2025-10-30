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
import java.io.OutputStream;
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
                Cell familiarCell = row.getCell(4);

                if (englishCell == null) {
                    continue; // 跳过空行
                }

                String english = getCellValue(englishCell).trim();
                String phonetic = phoneticCell != null ? getCellValue(phoneticCell).trim() : "";
                String chinese = chineseCell != null ? getCellValue(chineseCell).trim() : "";
                String example = exampleCell != null ? getCellValue(exampleCell).trim() : "";
                String familiar = familiarCell != null ? getCellValue(familiarCell).trim() : "";

                if (!english.isEmpty()) {
                    words.add(new Word(english, phonetic, chinese, example,"1".equals( familiar)));
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
    /**
 * 将单词列表导出到Excel文件
 * @param context 上下文
 * @param words 单词列表
 * @param uri 导出文件的URI
 * @throws IOException IO异常
 */
public static void exportWordsToExcel(Context context, List<Word> words, Uri uri) throws IOException {
    // 创建新的工作簿
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("单词表");

    // 创建表头行
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("英文");
    headerRow.createCell(1).setCellValue("音标");
    headerRow.createCell(2).setCellValue("中文释义");
    headerRow.createCell(3).setCellValue("例句");
    headerRow.createCell(4).setCellValue("熟悉程度"); // 1表示熟悉，0表示不熟悉

    // 填充数据
    for (int i = 0; i < words.size(); i++) {
        Word word = words.get(i);
        Row row = sheet.createRow(i + 1);

        row.createCell(0).setCellValue(word.getEnglish());
        row.createCell(1).setCellValue(word.getPhonetic());
        row.createCell(2).setCellValue(word.getChinese());
        row.createCell(3).setCellValue(word.getExample());
        row.createCell(4).setCellValue(word.isFamiliar() ? "1" : "0");
    }

    // 设置固定列宽（替代 autoSizeColumn 方法）
    sheet.setColumnWidth(0, 20 * 256); // 英文列
    sheet.setColumnWidth(1, 15 * 256); // 音标列
    sheet.setColumnWidth(2, 30 * 256); // 中文释义列
    sheet.setColumnWidth(3, 40 * 256); // 例句列
    sheet.setColumnWidth(4, 10 * 256); // 熟悉程度列


    // 写入文件
    OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
    if (outputStream != null) {
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}


}
