package pt.uc.dei.rest_api_robustness_tester.utils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class XlsxWriter implements Writer
{
    private final File destination;
    private final Map<String, List<String>> content;
    private String idColumn;
    
    public XlsxWriter(File destination)
    {
        this.destination = destination;
        this.content = new LinkedHashMap<>();
    }
    
    public Writer Add(String columnName, String value)
    {
        if(content.isEmpty())
            idColumn = columnName;
        
        if(!content.containsKey(columnName))
            content.put(columnName, new ArrayList<>());
        
        if(columnName.equals(idColumn))
            FillNonIdColumnsWithEmptyString();
    
        content.get(columnName).add(value);
        
        return this;
    }
    
    public Writer Write(String sheetName)
    {
        try
        {
            File f = destination;
            if(!f.getAbsolutePath().endsWith(".xlsx"))
                f = new File(f.getAbsolutePath() + ".xlsx");
            
            XSSFWorkbook workbook = f.exists()? new XSSFWorkbook(f) : new XSSFWorkbook();
    
            String safeSheetName = WorkbookUtil.createSafeSheetName(sheetName);
            
            int sheetIndex = workbook.getSheetIndex(safeSheetName);
            if(sheetIndex >= 0)
                workbook.removeSheetAt(sheetIndex);
            
            XSSFSheet sheet = workbook.createSheet(safeSheetName);
    
            if(sheetIndex >= 0)
                workbook.setSheetOrder(safeSheetName, sheetIndex);
            
            int c = 0, r = 0;   //sheet.getFirstRowNum();
            
            XSSFRow headerRow = sheet.createRow(r);
            for(String col : content.keySet())
                headerRow.createCell(c++).setCellValue(col);
            r++;
            
            for(int i = 0; i < GetMaximumRowCount(); i++)
            {
                c = 0;
                XSSFRow dataRow = sheet.createRow(r);
                for(String col : content.keySet())
                {
                    String value = i >= content.get(col).size()? "" : content.get(col).get(i);
                    dataRow.createCell(c++).setCellValue(TrimToExcel2007Limits(value));
                }
                r++;
            }
            
            FileOutputStream fOut = new FileOutputStream(f, true);
            
            workbook.write(fOut);
            fOut.close();
            workbook.close();
        }
        catch (IOException | InvalidFormatException e)
        {
            e.printStackTrace();
        }
        
        return this;
    }
    
    private int GetMaximumRowCount()
    {
        int maxRows = 0;
        
        for(String col : content.keySet())
            maxRows = Math.max(content.get(col).size(), maxRows);
        
        return maxRows;
    }
    
    private String TrimToExcel2007Limits(String value)
    {
        String suffix = "(...)";
        if(value.length() >= SpreadsheetVersion.EXCEL2007.getMaxTextLength())
            return value.substring(0, SpreadsheetVersion.EXCEL2007.getMaxTextLength() - suffix.length()) + suffix;
        return value;
    }
    
    private void FillNonIdColumnsWithEmptyString()
    {
        int lenIdColumn = content.get(idColumn).size();
        for(String columnName : content.keySet())
            if(!columnName.equals(idColumn) && content.get(columnName).size() < lenIdColumn)
                for(int i = 0; i < lenIdColumn - content.get(columnName).size(); i++)
                    content.get(columnName).add("");
    }
}
