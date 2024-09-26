package com.example.testserver;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

    @RestController
    @RequestMapping("/api/excel")
    public class ExcelToJsonController {

        @PostMapping("/upload")
        public ResponseEntity<List<Map<String, String>>> uploadExcelFile(@RequestParam("file") MultipartFile file) {
            try {
                // Convert the Excel file to JSON
                List<Map<String, String>> jsonData = readExcel(file);
                return ResponseEntity.ok(jsonData);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Collections.emptyList());
            }
        }

        // Function to read the Excel file and convert it to List<Map<String, String>>
        public List<Map<String, String>> readExcel(MultipartFile file) throws IOException {
            List<Map<String, String>> data = new ArrayList<>();

            // Open the Excel file from the MultipartFile
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);  // Read the first sheet

            Iterator<Row> rowIterator = sheet.iterator();
            Row headerRow = rowIterator.next();  // Assuming first row is header

            // Get headers from the first row
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(cell.getStringCellValue());
            }

            // Iterate over the rest of the rows and read cell values
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Map<String, String> rowData = new HashMap<>();

                for (int i = 0; i < headers.size(); i++) {
                    Cell cell = row.getCell(i);
                    if (cell != null) {
                        rowData.put(headers.get(i), getCellValue(cell));
                    } else {
                        rowData.put(headers.get(i), "");  // Handle empty cells
                    }
                }

                data.add(rowData);
            }

            workbook.close();

            return data;
        }

        // Helper method to extract cell values
        private String getCellValue(Cell cell) {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        return String.valueOf(cell.getNumericCellValue());
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        }

}
