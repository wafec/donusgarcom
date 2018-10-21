package donusgarcom.api.database.domain.business;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;

public class MenuDao extends GenericDao<MenuDao.Menu> {
    @Inject
    public MenuDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
                new SqlField("name", SqlFieldType.STRING),
                new SqlField("description", SqlFieldType.STRING),
                new SqlField("restaurantId", SqlFieldType.INT)
        };
    }

    @Override
    public String getTableName() {
        return "menus";
    }

    @Override
    public Class<Menu> getClazz() {
        return Menu.class;
    }

    public static class Menu extends GenericDao.GenericData {
        public String name;
        public String description;
        public int restaurantId;

        public Menu() {

        }

        public Menu(String name, String description, int restaurantId) {
            this.name = name;
            this.description = description;
            this.restaurantId = restaurantId;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setRestaurantId(int restaurantId) {
            this.restaurantId = restaurantId;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getRestaurantId() {
            return restaurantId;
        }
    }
}
