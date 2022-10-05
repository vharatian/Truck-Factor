package aserg.gtf.target;

import aserg.gtf.model.authorship.Developer;
import aserg.gtf.significance.SignificanceTFInfo;
import aserg.gtf.significance.TotalAuthorshipInfo;
import aserg.gtf.truckfactor.TFInfo;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class TargetInformationExporter {

    private static final Logger LOGGER = Logger.getLogger(TargetInformationExporter.class);

    public void export(List<TargetTFInfo> info, String path) throws Exception {
        LOGGER.info("Exporting " + info.size() + " target info to " + path);

        FileOutputStream out = new FileOutputStream(path);
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Name");

        XSSFFont headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 10);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        XSSFCellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setLeftBorderColor(IndexedColors.WHITE.getIndex());
        headerStyle.setRightBorderColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFRow headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints((short) 40);

        XSSFRow subHeaderRow = sheet.createRow(1);
        subHeaderRow.setHeightInPoints((short) 40);

        String[] titles = new String[]{
                "Target Path",
                "Significance Indicator",
                "Truck Factor",
                "Remaining Coverage",
                "Developers",
                "",
                ""
        };

        String[] subTitles = new String[]{
                "",
                "",
                "",
                "",
                "Name",
                "Number of Authored Files",
                "Authorship Coverage"
        };

        for (int i=0; i<titles.length; i++) {
            XSSFCell cell = headerRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(titles[i]);

            cell = subHeaderRow.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(subTitles[i]);
        }

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 6));
//        sheet.addMergedRegion(new CellRangeAddress(0, 0, 4, 6));



        XSSFFont dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 10);

        XSSFCellStyle pathStyle = workbook.createCellStyle();
        pathStyle.setAlignment(HorizontalAlignment.LEFT);
        pathStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        pathStyle.setFont(dataFont);

        XSSFCellStyle infoStyle = workbook.createCellStyle();
        infoStyle.setAlignment(HorizontalAlignment.CENTER);
        infoStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        infoStyle.setFont(dataFont);

        XSSFCellStyle developerInfo = workbook.createCellStyle();
        developerInfo.setAlignment(HorizontalAlignment.LEFT);
        developerInfo.setFont(dataFont);


        try {
            int rowNumber = 2;
            for (int targetPathIndex = 0; targetPathIndex < info.size(); targetPathIndex++) {
                TargetTFInfo targetTFInfo = info.get(targetPathIndex);
                List<XSSFRow> rows = new ArrayList<>();
                for (int significanceIndex = 0; significanceIndex < targetTFInfo.getInfo().size(); significanceIndex++) {
                    int firstRowIndex = rows.size();
                    TFInfo tfInfo = targetTFInfo.getInfo().get(significanceIndex);
                    if (tfInfo.getTf() <= 0) {
                        XSSFRow row = sheet.createRow(rowNumber++);
                        rows.add(row);

                        XSSFCell cell = row.createCell(1);
                        cell.setCellValue(tfInfo.getSignificanceIndicator());
                        continue;
                    }

                    if (tfInfo instanceof SignificanceTFInfo) {
                        SignificanceTFInfo significanceInfo = (SignificanceTFInfo) tfInfo;
                        for (int developerIndex = 0; developerIndex < significanceInfo.getAuthorshipInfos().size(); developerIndex++) {
                            TotalAuthorshipInfo authorshipInfo = significanceInfo.getAuthorshipInfos().get(developerIndex);
                            XSSFRow row = sheet.createRow(rowNumber++);
                            rows.add(row);

                            XSSFCell cell = row.createCell(4);
                            cell.setCellValue(authorshipInfo.getDeveloper().getName());
                            cell.setCellStyle(developerInfo);

                            cell = row.createCell(5);
                            cell.setCellValue(authorshipInfo.getNumberOfFile());
                            cell.setCellStyle(developerInfo);

                            cell = row.createCell(6);
                            cell.setCellValue(String.format("%.2f", authorshipInfo.getCoverage()));
                            cell.setCellStyle(developerInfo);
                        }
                    } else {
                        for (int developerIndex = 0; developerIndex < tfInfo.getTfDevelopers().size(); developerIndex++) {
                            Developer developer = tfInfo.getTfDevelopers().get(developerIndex);
                            XSSFRow row = sheet.createRow(rowNumber++);
                            rows.add(row);

                            XSSFCell cell = row.createCell(4);
                            cell.setCellValue(developer.getName());
                            cell.setCellStyle(developerInfo);

                            cell = row.createCell(5);
                            int devFiles = developer.getAuthorshipInfos().size();
                            cell.setCellValue(devFiles);
                            cell.setCellStyle(developerInfo);

                            cell = row.createCell(6);
                            cell.setCellValue(String.format("%.2f", (float) devFiles / tfInfo.getTotalFiles() * 100));
                            cell.setCellStyle(developerInfo);
                        }
                    }

                    XSSFRow infoRow = rows.get(firstRowIndex);
                    XSSFCell cell = infoRow.createCell(1);
                    cell.setCellValue(tfInfo.getSignificanceIndicator());
                    cell.setCellStyle(pathStyle);

                    cell = infoRow.createCell(2);
                    cell.setCellValue(tfInfo.getTf());
                    cell.setCellStyle(infoStyle);

                    cell = infoRow.createCell(3);
                    cell.setCellValue(String.format("%.2f", tfInfo.getCoverage()));
                    cell.setCellStyle(infoStyle);

                    if (rows.size() - firstRowIndex > 1) {
                        for (int i = 1; i <= 3; i++) {
                            sheet.addMergedRegion(new CellRangeAddress(infoRow.getRowNum(), rows.get(rows.size() - 1).getRowNum(), i, i));
                        }
                    }
                }

                XSSFCell cell = rows.get(0).createCell(0);
                String targetPath = targetTFInfo.getTargetPath();
                if (targetPath.isEmpty()) {
                    targetPath = "root";
                }
                cell.setCellValue(targetPath);
                cell.setCellStyle(pathStyle);

                sheet.addMergedRegion(new CellRangeAddress(rows.get(0).getRowNum(), rows.get(rows.size() - 1).getRowNum(), 0, 0));
            }

            for (int i = 0; i < 7; i++) {
                sheet.autoSizeColumn(i, true);
            }

            workbook.write(out);
        } catch (Exception e) {
            throw new Exception("Error while exporting infromation", e);
        } finally {
            workbook.close();
            out.close();
        }

    }
}
