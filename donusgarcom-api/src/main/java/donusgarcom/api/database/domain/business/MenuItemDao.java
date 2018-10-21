package donusgarcom.api.database.domain.business;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.domain.common.PriceDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;

public class MenuItemDao extends GenericDao<MenuItemDao.MenuItem> {
    @Inject
    public MenuItemDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return unionOfManagedSqlFields(new SqlField[] {
                    new SqlField("name", SqlFieldType.STRING)
                },
                normalizeSubManagedSqlFields("price.", new PriceDao().getManagedSqlFields())
        );
    }

    @Override
    public String getTableName() {
        return "menuitems";
    }

    @Override
    public Class<MenuItem> getClazz() {
        return MenuItem.class;
    }

    public static class MenuItem extends GenericDao.GenericData {
        public String name;
        public PriceDao.Price price;

        public MenuItem() {

        }

        public MenuItem(String name, PriceDao.Price price) {
            this.name = name;
            this.price = price;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPrice(PriceDao.Price price) {
            this.price = price;
        }

        public String getName() {
            return name;
        }

        public PriceDao.Price getPrice() {
            return price;
        }
    }
}
