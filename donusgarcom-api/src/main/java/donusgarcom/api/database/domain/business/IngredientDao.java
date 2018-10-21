package donusgarcom.api.database.domain.business;

import donusgarcom.api.database.domain.GenericDao;
import donusgarcom.api.database.util.DbManager;

import javax.inject.Inject;

public class IngredientDao extends GenericDao<IngredientDao.Ingredient> {
    @Inject
    public IngredientDao(DbManager dbManager) {
        super(dbManager);
    }

    @Override
    public String getTableName() {
        return "ingredients";
    }

    @Override
    public Class<Ingredient> getClazz() {
        return Ingredient.class;
    }

    @Override
    public SqlField[] getManagedSqlFields() {
        return new SqlField[] {
                new SqlField("name", SqlFieldType.INT),
                new SqlField("containsLactose", SqlFieldType.BOOLEAN),
                new SqlField("containsGluten", SqlFieldType.BOOLEAN),
                new SqlField("restaurantId", SqlFieldType.INT)
        };
    }

    public static class Ingredient extends GenericDao.GenericData {
        public String name;
        public boolean containsLactose;
        public boolean containsGluten;
        public int restaurantId;

        public Ingredient() {

        }

        public void setName(String name) {
            this.name = name;
        }

        public void setContainsLactose(boolean containsLactose) {
            this.containsLactose = containsLactose;
        }

        public void setContainsGluten(boolean containsGluten) {
            this.containsGluten = containsGluten;
        }

        public String getName() {
            return name;
        }

        public boolean getContainsLactose() {
            return containsLactose;
        }

        public boolean getContainsGluten() {
            return containsGluten;
        }

        public int getRestaurantId() {
            return restaurantId;
        }

        public void setRestaurantId(int restaurantId) {
            this.restaurantId = restaurantId;
        }
    }
}
