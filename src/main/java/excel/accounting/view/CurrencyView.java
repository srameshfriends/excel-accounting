package excel.accounting.view;

import excel.accounting.entity.Currency;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.CurrencyService;
import excel.accounting.shared.FileHelper;
import excel.accounting.ui.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Currency View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class CurrencyView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<Currency> readableTableView;
    private CurrencyService currencyService;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Registers, "currencyView", "Currency");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        currencyService = (CurrencyService) getService("currencyService");
        readableTableView = new ReadableTableView<Currency>().create();
        readableTableView.addTextColumn("code", "Currency").setPrefWidth(120);
        readableTableView.addTextColumn("name", "Name").setPrefWidth(260);
        readableTableView.addTextColumn("symbol", "Symbol").setPrefWidth(100);
        readableTableView.addTextColumn("decimalPrecision", "Precision").setMinWidth(120);
        readableTableView.addTextColumn("status", "Status").setMinWidth(120);
        readableTableView.addSelectionChangeListener(viewListener);
        readableTableView.setContextMenuHandler(viewListener);
        readableTableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        readableTableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        readableTableView.addContextMenuItem(closedActionId, "Update As Closed");
        readableTableView.addContextMenuItem(exportSelectedActionId, "Export Currency");
        readableTableView.addContextMenuItem(deleteActionId, "Delete Currency");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), readableTableView.getTableView());
        return basePanel;
    }

    private HBox createToolbar() {
        final String importActionId = "importAction", refreshActionId = "refreshAction";
        Button refreshBtn, importBtn, exportBtn;
        refreshBtn = createButton(refreshActionId, "Refresh", event -> loadRecords());
        importBtn = createButton(importActionId, "Import", event -> importFromExcelEvent());
        exportBtn = createButton(exportActionId, "Export", event -> exportToExcelEvent(exportActionId));
        //
        HBox box = new HBox();
        box.setSpacing(12);
        box.setPadding(new Insets(12));
        box.getChildren().addAll(refreshBtn, importBtn, exportBtn);
        return box;
    }

    @Override
    public boolean canCloseView() {
        return true;
    }

    @Override
    public void closeView() {
    }

    @Override
    public void openView(double width, double height) {
        onResize(width, height);
        onRowSelectionChanged(false);
        loadRecords();
    }

    @Override
    public void onWidthChanged(double width) {
        basePanel.setPrefWidth(width);
    }

    @Override
    public void onHeightChanged(double height) {
        basePanel.setPrefHeight(height);
    }

    private void statusChangedEvent(String actionId) {
        if (!confirmDialog("Change Status", "Are you really wish to change selected accounts Status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            currencyService.setAsConfirmed(readableTableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            currencyService.setAsDrafted(readableTableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            currencyService.setAsClosed(readableTableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        currencyService.deleteCurrency(readableTableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<Currency> accountList = currencyService.loadAll();
        if (accountList == null || accountList.isEmpty()) {
            return;
        }
        ObservableList<Currency> observableList = FXCollections.observableArrayList(accountList);
        readableTableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<Currency> readExcelData = new ReadExcelData<>("", file, currencyService);
        List<Currency> rowDataList = readExcelData.readRowData(currencyService.getColumnNames().length, true);
        if (rowDataList.isEmpty()) {
            return;
        }
        List<String> existingCodeList = currencyService.findCodeList();
        List<Currency> updateList = new ArrayList<>();
        List<Currency> insertList = new ArrayList<>();
        for (Currency currency : rowDataList) {
            if (existingCodeList.contains(currency.getCode())) {
                updateList.add(currency);
            } else {
                insertList.add(currency);
            }
        }
        if (!updateList.isEmpty()) {
            currencyService.updateCurrency(updateList);
        }
        if (!insertList.isEmpty()) {
            currencyService.insertCurrency(insertList);
        }
        loadRecords();
    }

    /*
    id, account_number, name, category, status, currency, balance, description
    */
    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "currency" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<Currency> writeExcelData = new WriteExcelData<>(actionId, file, currencyService);
        if (exportSelectedActionId.equals(actionId)) {
            List<Currency> selected = readableTableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(currencyService.loadAll());
        }
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        readableTableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
                closedActionId);
    }

    private void performActionEvent(final String actionId) {
        switch (actionId) {
            case deleteActionId:
                deleteEvent();
                break;
            case exportActionId:
            case exportSelectedActionId:
                exportToExcelEvent(actionId);
                break;
            case confirmedActionId:
            case draftedActionId:
            case closedActionId:
                statusChangedEvent(actionId);
                break;
        }
    }

    private class ViewListener implements ListChangeListener<Integer>, ActionHandler {
        @Override
        public void onChanged(Change<? extends Integer> change) {
            onRowSelectionChanged(0 < change.getList().size());
        }

        @Override
        public void onActionEvent(final String actionId) {
            performActionEvent(actionId);
        }
    }
}
