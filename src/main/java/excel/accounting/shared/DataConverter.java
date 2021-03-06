package excel.accounting.shared;

import excel.accounting.entity.AccountType;
import excel.accounting.entity.PaidStatus;
import excel.accounting.entity.Status;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Scanner;

/**
 * DataConverter
 */
public class DataConverter {
    private static final Logger logger = Logger.getLogger(DataConverter.class);
    private static DateFormat defaultDateFormat = new SimpleDateFormat("dd-mm-yyyy");

    public static int getInteger(Cell cell) {
        Double decimal = getDouble(cell);
        return decimal.intValue();
    }

    public static int getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            // ignore exception
        }
        return 0;
    }

    public static Status getStatus(Object status) {
        return status == null ? Status.Drafted : getEnum(Status.class, status.toString());
    }

    public static PaidStatus getPaidStatus(Object paidStatus) {
        return paidStatus == null ? null : getEnum(PaidStatus.class, paidStatus.toString());
    }

    public static AccountType getAccountType(Object accountType) {
        return accountType == null ? null : getEnum(AccountType.class, accountType.toString());
    }

    private static <E extends Enum<E>> E getEnum(Class<E> enumClass, String name) {
        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception ex) {
            logger.debug(ex.getMessage());
        }
        return null;
    }

    public static String getString(Cell cell) {
        if (CellType.STRING.equals(cell.getCellTypeEnum())) {
            return cell.getStringCellValue();
        } else if (CellType.NUMERIC.equals(cell.getCellTypeEnum())) {
            Double dle = cell.getNumericCellValue();
            return dle == 0 ? null : dle.toString();
        } else if (CellType.BOOLEAN.equals(cell.getCellTypeEnum())) {
            Boolean bln = cell.getBooleanCellValue();
            return bln.toString();
        } else if (CellType.ERROR.equals(cell.getCellTypeEnum())) {
            return null;
        }
        return null;
    }

    private static double getDouble(Cell cell) {
        double decimal;
        if (CellType.NUMERIC.equals(cell.getCellTypeEnum())) {
            decimal = cell.getNumericCellValue();
        } else {
            decimal = parseDouble(cell.getStringCellValue());
        }
        return decimal;
    }

    public static BigDecimal getBigDecimal(Cell cell) {
        return getBigDecimal(cell, 4);
    }

    public static Boolean getBoolean(Cell cell) {
        if (CellType.BOOLEAN.equals(cell.getCellTypeEnum())) {
            return cell.getBooleanCellValue();
        } else if (CellType.STRING.equals(cell.getCellTypeEnum())) {
            String value = cell.getStringCellValue();
            if (value != null && value.toLowerCase().equals("true")) {
                return Boolean.TRUE;
            }
        }
        return false;
    }

    private static BigDecimal getBigDecimal(Cell cell, int precision) {
        BigDecimal bigDecimal = new BigDecimal(getDouble(cell));
        return bigDecimal.setScale(precision, BigDecimal.ROUND_HALF_UP);
    }

    public static Date getDate(Cell cell) {
        if (CellType.NUMERIC.equals(cell.getCellTypeEnum())) {
            Double decimal = cell.getNumericCellValue();
            long dateTime = decimal.longValue();
            return dateTime == 0 ? null : new Date(dateTime);
        } else if (HSSFDateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        } else {
            try {
                String value = cell.getStringCellValue();
                if (value != null) {
                    return defaultDateFormat.parse(value);
                }
            } catch (Exception ex) {
                ex.getMessage();
            }
        }
        return null;
    }

    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            ex.getMessage();
        }
        return 0;
    }

    public static String encode(String text) {
        try {
            Base64.Encoder encoder = Base64.getEncoder();
            byte[] encodedBytes = encoder.encode(text.getBytes());
            return new String(encodedBytes);
        } catch (Exception ex) {
            /// ignore exception
        }
        return null;
    }

    public static String decode(String text) {
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodeBytes = decoder.decode(text.getBytes());
            return new String(decodeBytes);
        } catch (Exception ex) {
            /// ignore exception
        }
        return null;
    }

    public static String getSystemSerialNumber() throws Throwable {
        // wmic command for diskdrive id: wmic DISKDRIVE GET SerialNumber
        // wmic command for cpu id : wmic cpu get ProcessorId
        Process process = Runtime.getRuntime().exec(new String[]{"wmic", "bios", "get", "serialnumber"});
        process.getOutputStream().close();
        Scanner scanner = new Scanner(process.getInputStream());
        return scanner.next();
    }

    public static String getUniqueFileName(String fileName, String extension) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd-HHmm");
        String suffix = simpleDateFormat.format(new Date());
        return fileName + "-" + suffix + "." + extension;
    }
}
