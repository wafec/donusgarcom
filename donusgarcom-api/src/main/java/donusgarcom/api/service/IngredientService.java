package donusgarcom.api.service;

import donusgarcom.api.database.domain.business.IngredientDao;

import javax.inject.Inject;

public class IngredientService extends GenericService {
    IngredientDao ingredientDao;

    @Inject
    public IngredientService(IngredientDao ingredientDao) {
        this.ingredientDao = ingredientDao;
    }
}
