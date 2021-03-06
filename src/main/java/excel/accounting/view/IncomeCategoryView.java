package excel.accounting.view;

import excel.accounting.dao.IncomeCategoryDao;
import excel.accounting.entity.IncomeCategory;
import excel.accounting.poi.ReadExcelData;
import excel.accounting.poi.WriteExcelData;
import excel.accounting.service.IncomeCategoryService;
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
 * Income Category View
 *
 * @author Ramesh
 * @since Oct, 2016
 */
public class IncomeCategoryView extends AbstractView implements ViewHolder {
    private final String exportActionId = "exportAction", deleteActionId = "deleteAction";
    private final String exportSelectedActionId = "exportSelectedAction";
    private final String confirmedActionId = "confirmedAction";
    private final String closedActionId = "closedAction", draftedActionId = "draftedAction";

    private ReadableTableView<IncomeCategory> tableView;
    private IncomeCategoryService incomeCategoryService;
    private IncomeCategoryDao incomeCategoryDao;
    private VBox basePanel;

    @Override
    public ViewConfig getViewConfig() {
        return new ViewConfig(ViewGroup.Income, "incomeCategoryView", "Income Category");
    }

    @Override
    public Node createControl() {
        ViewListener viewListener = new ViewListener();
        incomeCategoryService = (IncomeCategoryService) getService("incomeCategoryService");
        tableView = new ReadableTableView<IncomeCategory>().create();
        tableView.addTextColumn("code", "Category Code").setPrefWidth(120);
        tableView.addTextColumn("name", "Name").setPrefWidth(200);
        tableView.addTextColumn("description", "Description").setPrefWidth(280);
        tableView.addTextColumn("currency", "Currency").setMinWidth(60);
        tableView.addTextColumn("incomeAccount", "Income Account").setMinWidth(210);
        tableView.addTextColumn("status", "Status").setMinWidth(80);
        tableView.addSelectionChangeListener(viewListener);
        tableView.setContextMenuHandler(viewListener);
        tableView.addContextMenuItem(draftedActionId, "Update As Drafted");
        tableView.addContextMenuItem(confirmedActionId, "Update As Confirmed");
        tableView.addContextMenuItem(closedActionId, "Update As Closed");
        tableView.addContextMenuItem(exportSelectedActionId, "Export Income Categories");
        tableView.addContextMenuItem(deleteActionId, "Delete Income Categories");
        //
        basePanel = new VBox();
        basePanel.getChildren().addAll(createToolbar(), tableView.getTableView());
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
        if (!confirmDialog("Are you really wish to change status?")) {
            return;
        }
        if (confirmedActionId.equals(actionId)) {
            incomeCategoryService.setAsConfirmed(tableView.getSelectedItems());
        } else if (draftedActionId.equals(actionId)) {
            incomeCategoryService.setAsDrafted(tableView.getSelectedItems());
        } else if (closedActionId.equals(actionId)) {
            incomeCategoryService.setAsClosed(tableView.getSelectedItems());
        }
        loadRecords();
    }

    private void deleteEvent() {
        incomeCategoryService.deleteIncomeCategory(tableView.getSelectedItems());
        loadRecords();
    }

    private void loadRecords() {
        List<IncomeCategory> categoryList = incomeCategoryDao.loadAll(IncomeCategory.class);
        if (categoryList == null || categoryList.isEmpty()) {
            return;
        }
        ObservableList<IncomeCategory> observableList = FXCollections.observableArrayList(categoryList);
        tableView.setItems(observableList);
    }

    private void importFromExcelEvent() {
        File file = FileHelper.showOpenFileDialogExcel(getPrimaryStage());
        if (file == null) {
            return;
        }
        ReadExcelData<IncomeCategory> readExcelData = new ReadExcelData<>("", file, incomeCategoryService);
        List<IncomeCategory> dataList = readExcelData.readRowData(incomeCategoryService.getColumnNames().length, true);
        if (dataList.isEmpty()) {
            return;
        }
        List<String> existingCodeList = incomeCategoryDao.loadCodeList();
        List<IncomeCategory> updateList = new ArrayList<>();
        List<IncomeCategory> insertList = new ArrayList<>();
        for (IncomeCategory category : dataList) {
            if (existingCodeList.contains(category.getCode())) {
                updateList.add(category);
            } else {
                insertList.add(category);
            }
        }
        if (!updateList.isEmpty()) {
            incomeCategoryService.updateIncomeCategory(updateList);
        }
        if (!insertList.isEmpty()) {
            incomeCategoryService.insertIncomeCategory(insertList);
        }
        loadRecords();
    }

    /*
    id, code, name, status, currency, credit_account, debit_account, description
    */
    private void exportToExcelEvent(final String actionId) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmm");
        String fileName = simpleDateFormat.format(new Date());
        fileName = "income-category" + fileName + ".xls";
        File file = FileHelper.showSaveFileDialogExcel(fileName, getPrimaryStage());
        if (file == null) {
            return;
        }
        WriteExcelData<IncomeCategory> writeExcelData = new WriteExcelData<>(actionId, file, incomeCategoryService);
        if (exportSelectedActionId.equals(actionId)) {
            List<IncomeCategory> selected = tableView.getSelectedItems();
            writeExcelData.writeRowData(selected);
        } else {
            writeExcelData.writeRowData(incomeCategoryDao.loadAll(IncomeCategory.class));
        }
    }

    private void onRowSelectionChanged(boolean isRowSelected) {
        tableView.setDisable(!isRowSelected, exportSelectedActionId, draftedActionId, confirmedActionId,
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
