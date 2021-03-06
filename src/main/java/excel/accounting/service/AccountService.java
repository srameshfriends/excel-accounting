package excel.accounting.service;

import excel.accounting.dao.AccountDao;
import excel.accounting.dao.CurrencyDao;
import excel.accounting.entity.Account;
import excel.accounting.entity.AccountType;
import excel.accounting.entity.Currency;
import excel.accounting.entity.Status;
import excel.accounting.poi.ExcelTypeConverter;
import excel.accounting.shared.DataConverter;
import excel.accounting.shared.RulesType;
import excel.accounting.shared.StringRules;
import org.apache.poi.ss.usermodel.Cell;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Account Service
 */
public class AccountService extends AbstractService implements ExcelTypeConverter<Account> {
    private AccountDao accountDao;
    private CurrencyDao currencyDao;

    private AccountDao getAccountDao() {
        if (accountDao == null) {
            accountDao = (AccountDao) getBean("accountDao");
        }
        return accountDao;
    }

    private CurrencyDao getCurrencyDao() {
        if (currencyDao == null) {
            currencyDao = (CurrencyDao) getBean("currencyDao");
        }
        return currencyDao;
    }

    private List<AccountType> getAccountTypeList() {
        List<AccountType> accountTypeList = new ArrayList<>();
        Collections.addAll(accountTypeList, AccountType.values());
        return accountTypeList;
    }

    private void updateStatus(List<Account> accountList) {
        /*OrmTransaction transaction = createOrmTransaction();
        try {
            for (Account account : accountList) {
                transaction.update(account);
            }
            commitBatch(transaction);
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }

    public void updateCurrency(Currency currency, List<Account> accountList) {
      /*  List<Account> filteredList = filteredByStatus(Status.Drafted, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Error : Only drafted accounts are allowed to change currency");
            return;
        }
        final String currencyCode = currency == null ? null : currency.getCode();
        for (Account account : filteredList) {
            account.setCurrency(currencyCode);
        }
        QueryBuilder queryBuilder = getQueryBuilder("updateCurrency");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : filteredList) {
            executeBatch(100, ).addBatch(getColumnsMap("updateCurrency", account));
        }
        executeBatch(transaction);*/
    }

    public void updateAccountType(AccountType accountType, List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Drafted, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Error : Only drafted accounts are allowed to modify");
            return;
        }
        for (Account account : filteredList) {
            account.setAccountType(accountType);
        }
       /* QueryBuilder queryBuilder = getQueryBuilder("updateAccountType");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : filteredList) {
            transaction.addBatch(getColumnsMap("updateAccountType", account));
        }
        executeBatch(transaction);*/
    }

    public void setAsDrafted(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Confirmed, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Wrong Status : confirmed accounts are allowed to modify as drafted");
            return;
        }
        try {
            for (Account account : filteredList) {
                Object errorMessage = getSqlReader().getUsedReference(Account.class, account.getCode());
                if (errorMessage != null) {
                    showMessage(errorMessage.toString());
                    return;
                }
                account.setStatus(Status.Drafted);
            }
            updateStatus(filteredList);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void setAsConfirmed(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Drafted, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Error : Only drafted accounts are allowed to confirm");
            return;
        }
        List<Account> validList = new ArrayList<>();
        filteredList.stream().filter(this::confirmValidate).forEach(account -> {
            account.setStatus(Status.Confirmed);
            validList.add(account);
        });
        if (validList.isEmpty()) {
            showMessage("Error : valid drafted accounts not found");
            return;
        }
        updateStatus(validList);
    }

    public void setAsClosed(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Confirmed, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Wrong Status : Only confirmed accounts should be closed");
            return;
        }
        for (Account account : filteredList) {
            account.setStatus(Status.Closed);
        }
        updateStatus(filteredList);
    }

    public void reopenAccount(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Closed, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Wrong Status : closed accounts are allowed to reopen");
            return;
        }
        for (Account account : filteredList) {
            account.setStatus(Status.Confirmed);
        }
        updateStatus(filteredList);
    }

    private boolean confirmValidate(Account account) {
        return !(account.getCode() == null || account.getCurrency() == null ||
                account.getAccountType() == null || account.getName() == null);
    }

    private boolean insertValidate(Account account, StringRules rules) {
        return rules.isValid(account.getCode()) && !StringRules.isEmpty(account.getName());
    }

    public void insertAccount(List<Account> accountList) {
        showMessage("Account number, name should not be empty");
        StringRules rules = new StringRules();
        rules.setMinMaxLength(3, 6);
        rules.setFirstCharAlphaOnly(true);
        rules.setRulesType(RulesType.Alphanumeric);
        //
        List<String> existingList = getAccountDao().loadCodeList();
        List<Account> validList = new ArrayList<>();
        for (Account account : accountList) {
            if (insertValidate(account, rules) && !existingList.contains(account.getCode())) {
                validList.add(account);
            }
        }
        if (validList.isEmpty()) {
            showMessage("Valid accounts not found");
            return;
        }
        List<String> currencyList = new ArrayList<>(); //getCurrencyDao().findCodeList();
        List<AccountType> accountTypeList = getAccountTypeList();
       /* QueryBuilder queryBuilder = getQueryBuilder("insertAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : validList) {
            if (account.getAccountType() != null && !accountTypeList.contains(account.getAccountType())) {
                account.setAccountType(null);
            }
            if (account.getCurrency() != null && !currencyList.contains(account.getCurrency())) {
                account.setCurrency(null);
            }
            transaction.addBatch(getColumnsMap("insertAccount", account));
        }
        executeBatch(transaction);*/
    }

    public void deleteAccount(List<Account> accountList) {
        List<Account> filteredList = filteredByStatus(Status.Drafted, accountList);
        if (filteredList.isEmpty()) {
            showMessage("Error : Drafted accounts not found to delete");
            return;
        }
       /* QueryBuilder queryBuilder = getQueryBuilder("deleteAccount");
        Transaction transaction = createTransaction();
        transaction.setBatchQuery(queryBuilder);
        for (Account account : filteredList) {
            transaction.addBatch(getColumnsMap("deleteAccount", account));
        }
        executeBatch(transaction);*/
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"Account Number", "Name", "Account Type", "Status", "Currency", "Balance", "Description"};
    }

    /**
     * code, name, account_type, status, currency, description
     */
    @Override
    public Account getExcelType(String type, Cell[] array) {
        Account account = new Account();
        account.setCode(DataConverter.getString(array[0]));
        account.setName(DataConverter.getString(array[1]));
        account.setAccountType(DataConverter.getAccountType(array[2]));
        account.setStatus(DataConverter.getStatus(array[3]));
        account.setCurrency(DataConverter.getString(array[4]));
        account.setBalance(DataConverter.getBigDecimal(array[5]));
        account.setDescription(DataConverter.getString(array[6]));
        return account;
    }

    /**
     * code, name, account_type, status, currency, balance, description
     */
    @Override
    public Object[] getExcelRow(String type, Account account) {
        Object[] cellData = new Object[7];
        cellData[0] = account.getCode();
        cellData[1] = account.getName();
        cellData[2] = account.getAccountType();
        cellData[3] = account.getStatus().toString();
        cellData[4] = account.getCurrency();
        cellData[5] = account.getBalance();
        cellData[6] = account.getDescription();
        return cellData;
    }

    private List<Account> filteredByStatus(Status status, List<Account> accList) {
        return accList.stream().filter(acc -> status.equals(acc.getStatus())).collect(Collectors.toList());
    }
}
