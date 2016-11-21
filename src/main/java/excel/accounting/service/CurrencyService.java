package excel.accounting.service;

import excel.accounting.db.*;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.CurrencyDao;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.RulesType;
import excel.accounting.shared.StringRules;
import org.apache.poi.ss.usermodel.Cell;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Currency Service
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyService extends AbstractService implements EntityToRowColumns<Currency>,
        ExcelTypeConverter<Currency>, SqlWriteResponse {
    private CurrencyDao currencyDao;

    @Override
    protected String getSqlFileName() {
        return "currency";
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    private void updateStatus(List<Currency> currencyList) {
        /*OrmTransaction transaction = createOrmTransaction();
        try {
            for (Currency currency : currencyList) {
                transaction.update(currency);
            }
            commitBatch(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public void setAsDrafted(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as drafted");
            return;
        }
        for (Currency currency : filteredList) {
            String errorMessage = getCurrencyDao().isEntityReferenceUsed(currency.getCode());
            if (errorMessage != null) {
                setMessage(errorMessage);
                return;
            }
            currency.setStatus(Status.Drafted);
        }
        updateStatus(filteredList);
    }

    public void setAsConfirmed(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : drafted currency are allowed to modify as confirmed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList);
    }

    public void setAsClosed(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Confirmed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : confirmed currency are allowed to modify as closed");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Closed);
        }
        updateStatus(filteredList);
    }

    public void reopenCurrency(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Closed, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Wrong Status : closed currency are allowed to reopen");
            return;
        }
        for (Currency currency : filteredList) {
            currency.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList);
    }

    private boolean insertValid(Currency currency, StringRules rules) {
        return rules.isValid(currency.getCode()) && !StringRules.isEmpty(currency.getName());
    }

    public void insertCurrency(List<Currency> currencyList) {
        setMessage("Currency code, name should not be empty");
        StringRules rules = new StringRules();
        rules.setMinMaxLength(3, 3);
        rules.setRulesType(RulesType.AlphaOnly);
        //
        List<Currency> validList = currencyList.stream().filter(currency ->
                insertValid(currency, rules)).collect(Collectors.toList());
        if (validList.isEmpty()) {
            setMessage("Valid currency not found");
        } else {
            insert(100, currencyList, this);
        }
    }

    public void deleteCurrency(List<Currency> currencyList) {
        List<Currency> filteredList = filteredByStatus(Status.Drafted, currencyList);
        if (filteredList.isEmpty()) {
            setMessage("Error : Only drafted currency allowed to delete");
        }
        /*OrmTransaction transaction = createOrmTransaction();
        try {
            for (Currency currency : filteredList) {
                transaction.delete(currency);
            }
            commitBatch(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    @Override
    public Map<Integer, Object> getColumnsMap(final String queryName, Currency entity) {
        Map<Integer, Object> map = new HashMap<>();
        if ("insertCurrency".equals(queryName)) {
            map.put(1, entity.getCode());
            map.put(2, entity.getName());
            map.put(3, entity.getDecimalPrecision());
            map.put(4, entity.getSymbol());
            map.put(5, Status.Drafted.toString());
        } else if ("deleteCurrency".equals(queryName)) {
            map.put(1, entity.getCode());
        } else if ("updateStatus".equals(queryName)) {
            map.put(1, entity.getStatus().toString());
            map.put(2, entity.getCode());
        }
        return map;
    }

    @Override
    public void onSqlResponse(int processId) {
    }

    @Override
    public void onSqlError(int processId, SQLException ex) {
        ex.printStackTrace();
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Code", "Name", "Precision", "Symbol", "Status"};
    }

    @Override
    public Currency getExcelType(String type, Cell[] array) {
        Currency currency = new Currency();
        currency.setCode(DataConverter.getString(array[0]));
        currency.setName(DataConverter.getString(array[1]));
        currency.setDecimalPrecision(DataConverter.getInteger(array[2]));
        currency.setSymbol(DataConverter.getString(array[3]));
        currency.setStatus(DataConverter.getStatus(array[4]));
        return currency;
    }

    @Override
    public Object[] getExcelRow(String type, Currency currency) {
        Object[] cellData = new Object[5];
        cellData[0] = currency.getCode();
        cellData[1] = currency.getName();
        cellData[2] = currency.getDecimalPrecision();
        cellData[3] = currency.getSymbol();
        cellData[4] = currency.getStatus().toString();
        return cellData;
    }

    private List<Currency> filteredByStatus(Status status, List<Currency> curList) {
        return curList.stream().filter(cur -> status.equals(cur.getStatus())).collect(Collectors.toList());
    }
}
