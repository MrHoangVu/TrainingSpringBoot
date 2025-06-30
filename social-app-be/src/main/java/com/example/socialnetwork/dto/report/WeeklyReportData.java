package com.example.socialnetwork.dto.report;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.HeadFontStyle;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@HeadFontStyle(fontHeightInPoints = 14)
public class WeeklyReportData {

    @ExcelProperty("Metric")
    @ColumnWidth(35)
    private String metric;

    @ExcelProperty("Count")
    @ColumnWidth(15)
    private Long count;
}