package uc.mainframe.migration.regression.customer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class CustomerListGenerator {

    private static final String CUSTOMER_ID_COLUMN_NAME = "customerId";

    private static final Pattern CUSTOMER_ID_PATTERN =
            Pattern.compile("\\d{12}");

    public List<CustomerRecord> generateCustomerList(
            InputStream workbookInputStream) throws IOException {

        Objects.requireNonNull(
                workbookInputStream,
                "Workbook input stream must not be null");

        try (Workbook workbook =
                     WorkbookFactory.create(workbookInputStream)) {

            Sheet customerSheet = getFirstSheet(workbook);
            Row headerRow = getHeaderRow(customerSheet);

            DataFormatter dataFormatter = new DataFormatter();

            int customerIdColumnIndex =
                    findCustomerIdColumnIndex(
                            headerRow,
                            dataFormatter);

            List<CustomerRecord> customerRecords =
                    new ArrayList<>();

            List<String> invalidCustomerIds =
                    new ArrayList<>();

            readCustomerRows(
                    customerSheet,
                    headerRow.getRowNum(),
                    customerIdColumnIndex,
                    dataFormatter,
                    customerRecords,
                    invalidCustomerIds);

            if (!invalidCustomerIds.isEmpty()) {
                throw new IllegalArgumentException(
                        "Customer list is not valid. Invalid customer IDs: "
                                + String.join(", ", invalidCustomerIds));
            }

            if (customerRecords.isEmpty()) {
                throw new IllegalArgumentException(
                        "The customer workbook contains no customer IDs");
            }

            return List.copyOf(customerRecords);
        }
    }

    private Sheet getFirstSheet(Workbook workbook) {
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalArgumentException(
                    "The customer workbook contains no worksheets");
        }

        return workbook.getSheetAt(0);
    }

    private Row getHeaderRow(Sheet customerSheet) {
        Row headerRow = customerSheet.getRow(
                customerSheet.getFirstRowNum());

        if (headerRow == null) {
            throw new IllegalArgumentException(
                    "The customer worksheet contains no header row");
        }

        return headerRow;
    }

    private int findCustomerIdColumnIndex(
            Row headerRow,
            DataFormatter dataFormatter) {

        for (Cell headerCell : headerRow) {
            String columnName = dataFormatter
                    .formatCellValue(headerCell)
                    .trim();

            if (CUSTOMER_ID_COLUMN_NAME.equalsIgnoreCase(
                    columnName)) {
                return headerCell.getColumnIndex();
            }
        }

        throw new IllegalArgumentException(
                "Required Excel column 'customerId' was not found");
    }

    private void readCustomerRows(
            Sheet customerSheet,
            int headerRowIndex,
            int customerIdColumnIndex,
            DataFormatter dataFormatter,
            List<CustomerRecord> customerRecords,
            List<String> invalidCustomerIds) {

        for (int rowIndex = headerRowIndex + 1;
             rowIndex <= customerSheet.getLastRowNum();
             rowIndex++) {

            Row customerRow = customerSheet.getRow(rowIndex);

            if (customerRow == null) {
                continue;
            }

            String customerId = readCustomerId(
                    customerRow,
                    customerIdColumnIndex,
                    dataFormatter);

            if (customerId.isBlank()) {
                continue;
            }

            if (isValidCustomerId(customerId)) {
                customerRecords.add(
                        new CustomerRecord(customerId));
            } else {
                invalidCustomerIds.add(customerId);
            }
        }
    }

    private String readCustomerId(
            Row customerRow,
            int customerIdColumnIndex,
            DataFormatter dataFormatter) {

        Cell customerIdCell = customerRow.getCell(
                customerIdColumnIndex,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (customerIdCell == null) {
            return "";
        }

        return dataFormatter
                .formatCellValue(customerIdCell)
                .trim();
    }

    private boolean isValidCustomerId(String customerId) {
        return CUSTOMER_ID_PATTERN
                .matcher(customerId)
                .matches();
    }
}