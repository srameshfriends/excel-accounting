package excel.accounting.service;

import excel.accounting.entity.BankTransaction;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.dao.BankTransactionDao;
import excel.accounting.shared.DataConverter;
import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Bank Transaction Service
 *
 * @author Ramesh
 * @since Oct 2016
 */
public class BankTransactionService extends AbstractService implements ExcelTypeConverter<BankTransaction> {
    private BankTransactionDao bankTransactionDao;

    private BankTransactionDao getBankTransactionDao() {
        if (bankTransactionDao == null) {
            bankTransactionDao = (BankTransactionDao) getBean("bankTransactionDao");
        }
        return bankTransactionDao;
    }

    /**
     * status, code
     */
    private void updateStatus(Status requiredStatus, Status changedStatus, List<BankTransaction> transactionList) {
        List<BankTransaction> filteredList = filteredByStatus(requiredStatus, transactionList);
        if (filteredList.isEmpty()) {
            return;
        }
        for (BankTransaction bankTransaction : filteredList) {
            bankTransaction.setStatus(changedStatus);
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("updateStatus");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (BankTransaction bankTransaction : filteredList) {
            transaction.addBatch(getColumnsMap("updateStatus", bankTransaction));
        }
        executeBatch(transaction);*/
    }

    public void setAsDrafted(List<BankTransaction> bankTransactionList) {
        updateStatus(Status.Confirmed, Status.Drafted, bankTransactionList);
    }

    public void setAsConfirmed(List<BankTransaction> bankTransactionList) {
        updateStatus(Status.Drafted, Status.Confirmed, bankTransactionList);
    }

    public void setAsClosed(List<BankTransaction> bankTransactionList) {
        updateStatus(Status.Confirmed, Status.Closed, bankTransactionList);
    }

    public void insertBankTransaction(List<BankTransaction> bankTransactionList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("insertBankTransaction");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (BankTransaction bankTransaction : bankTransactionList) {
            transaction.addBatch(getColumnsMap("insertBankTransaction", bankTransaction));
        }
        executeBatch(transaction);*/
    }

    public void updateBankTransaction(List<BankTransaction> bankTransactionList) {
        /*QueryBuilder queryBuilder = getQueryBuilder("updateBankTransaction");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (BankTransaction bankTransaction : bankTransactionList) {
            transaction.addBatch(getColumnsMap("updateBankTransaction", bankTransaction));
        }
        executeBatch(transaction);*/
    }

    public void deleteBankTransaction(List<BankTransaction> bankTransactionList) {
        List<BankTransaction> filteredList = filteredByStatus(Status.Drafted, bankTransactionList);
        if (filteredList.isEmpty()) {
            return;
        }
        /*QueryBuilder queryBuilder = getQueryBuilder("deleteBankTransaction");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (BankTransaction bankTransaction : filteredList) {
            transaction.addBatch(getColumnsMap("deleteBankTransaction", bankTransaction));
        }
        executeBatch(transaction);*/
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"id", "Bank Code", "Date", "Index", "Transaction Code", "Description", "Currency",
                "Credit Account", "Debit Amount", "Status"};
    }

    @Override
    public BankTransaction getExcelType(String type, Cell[] array) {
        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setCode(DataConverter.getString(array[0]));
        bankTransaction.setBank(DataConverter.getString(array[1]));
        bankTransaction.setTransactionDate(DataConverter.getDate(array[2]));
        bankTransaction.setTransactionIndex(DataConverter.getInteger(array[3]));
        bankTransaction.setTransactionCode(DataConverter.getString(array[4]));
        bankTransaction.setDescription(DataConverter.getString(array[5]));
        bankTransaction.setCurrency(DataConverter.getString(array[6]));
        bankTransaction.setCreditAmount(DataConverter.getBigDecimal(array[7]));
        bankTransaction.setDebitAmount(DataConverter.getBigDecimal(array[8]));
        bankTransaction.setStatus(DataConverter.getStatus(array[9]));
        return bankTransaction;
    }

    @Override
    public Object[] getExcelRow(String type, BankTransaction bankTransaction) {
        Object[] cellData = new Object[10];
        cellData[0] = bankTransaction.getCode();
        cellData[1] = bankTransaction.getBank();
        cellData[2] = bankTransaction.getTransactionDate();
        cellData[3] = bankTransaction.getTransactionIndex();
        cellData[4] = bankTransaction.getTransactionCode();
        cellData[5] = bankTransaction.getDescription();
        cellData[6] = bankTransaction.getCurrency();
        cellData[7] = bankTransaction.getCreditAmount();
        cellData[8] = bankTransaction.getDebitAmount();
        cellData[9] = bankTransaction.getStatus().toString();
        return cellData;
    }

    private List<BankTransaction> filteredByStatus(Status status, List<BankTransaction> tranList) {
        return tranList.stream().filter(bank -> status.equals(bank.getStatus())).collect(Collectors.toList());
    }
}
