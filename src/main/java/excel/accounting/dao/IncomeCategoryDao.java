package excel.accounting.dao;

import excel.accounting.db.AbstractDao;
import excel.accounting.entity.IncomeCategory;

/**
 * Income Category Dao
 *
 * @author Ramesh
 * @since Nov, 2016
 */
public class IncomeCategoryDao extends AbstractDao<IncomeCategory> {

    @Override
    protected IncomeCategory getReferenceRow(String primaryKay) {

        return null;
    }
}