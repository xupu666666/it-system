package com.ict.system.util;

import com.ict.system.model.MaintenanceOrder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExportUtil {
    public static byte[] generateMaintenanceExcel(List<MaintenanceOrder> orders) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("维修单明细");

        try {
            // 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 创建数字单元格样式
            CellStyle numberStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            numberStyle.setDataFormat(format.getFormat("#,##0.00"));

            // 创建日期单元格样式
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(format.getFormat("yyyy-mm-dd hh:mm:ss"));

            // 表头
            String[] headers = {"维修单号", "标题", "描述", "申请人", "部门", "维修类型", "设备编号", "供应商", "处理人", "状态", "维修金额", "创建时间", "更新时间", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 数据行
            int rowIdx = 1;
            for (MaintenanceOrder order : orders) {
                try {
                    Row row = sheet.createRow(rowIdx++);

                    // 安全地设置字符串值
                    setCellStringValue(row.createCell(0), order.getOrderNo());
                    setCellStringValue(row.createCell(1), order.getTitle());
                    setCellStringValue(row.createCell(2), order.getDescription());
                    setCellStringValue(row.createCell(3), order.getRequester());
                    setCellStringValue(row.createCell(4), order.getRequesterDepartment());
                    setCellStringValue(row.createCell(5), order.getAssetType());
                    setCellStringValue(row.createCell(6), order.getAssetId());
                    setCellStringValue(row.createCell(7), order.getSupplier());
                    setCellStringValue(row.createCell(8), order.getAssignee());
                    setCellStringValue(row.createCell(9), order.getStatus());

                    // 设置数字值
                    Cell costCell = row.createCell(10);
                    if (order.getCost() != null) {
                        costCell.setCellValue(order.getCost());
                        costCell.setCellStyle(numberStyle);
                    } else {
                        costCell.setCellValue(0.0);
                        costCell.setCellStyle(numberStyle);
                    }

                    // 设置日期值
                    Cell createdAtCell = row.createCell(11);
                    if (order.getCreatedAt() != null) {
                        createdAtCell.setCellValue(order.getCreatedAt());
                        createdAtCell.setCellStyle(dateStyle);
                    } else {
                        createdAtCell.setCellValue("");
                    }

                    Cell updatedAtCell = row.createCell(12);
                    if (order.getUpdatedAt() != null) {
                        updatedAtCell.setCellValue(order.getUpdatedAt());
                        updatedAtCell.setCellStyle(dateStyle);
                    } else {
                        updatedAtCell.setCellValue("");
                    }

                    setCellStringValue(row.createCell(13), order.getNotes());
                } catch (Exception e) {
                    System.err.println("处理维修单数据时出错: " + e.getMessage());
                    // 继续处理下一行，不中断整个过程
                }
            }

            // 自动列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                // 设置最小列宽
                if (sheet.getColumnWidth(i) < 3000) {
                    sheet.setColumnWidth(i, 3000);
                }
                // 设置最大列宽
                if (sheet.getColumnWidth(i) > 10000) {
                    sheet.setColumnWidth(i, 10000);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } finally {
            // 确保工作簿被关闭
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("关闭Excel工作簿时出错: " + e.getMessage());
            }
        }
    }

    // 安全地设置字符串值的辅助方法
    private static void setCellStringValue(Cell cell, String value) {
        cell.setCellValue(value != null ? value : "");
    }
}