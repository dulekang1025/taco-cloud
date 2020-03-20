package tacos.data;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementCreatorFactory;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;


import tacos.Ingredient;
import tacos.Taco;
import tacos.web.IngredientByIdConverter;

@Repository
public class JdbcTacoRepository implements TacoRepository{
    private JdbcTemplate jdbc;
    private IngredientByIdConverter ingredientByIdConverter;

    @Autowired
    public JdbcTacoRepository(JdbcTemplate jdbc, IngredientByIdConverter ingredientByIdConverter){

        this.jdbc = jdbc;
        this.ingredientByIdConverter = ingredientByIdConverter;
    }
    @Override
    public Taco save(Taco taco){
        long tacoId = saveTacoInfo(taco);
        taco.setId(tacoId);
        for(String ingredient_string : taco.getIngredients()){
            saveIngredientToTaco(ingredientByIdConverter.convert(ingredient_string), tacoId);
        }
        return taco;
    }

    private long saveTacoInfo(Taco taco){
        // set time
        taco.setCreatedAt(new Date());
        PreparedStatementCreatorFactory preparedStatementCreatorFactory = new PreparedStatementCreatorFactory(
                "insert into Taco (name, createdAt) values (?, ?)",
                Types.VARCHAR, Types.TIMESTAMP
        );

// By default, returnGeneratedKeys = false so change it to true
        preparedStatementCreatorFactory.setReturnGeneratedKeys(true);

        PreparedStatementCreator psc =
                preparedStatementCreatorFactory.newPreparedStatementCreator(
                        Arrays.asList(
                                taco.getName(),
                                new Timestamp(taco.getCreatedAt().getTime())));
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(psc,keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void saveIngredientToTaco(Ingredient ingredient,
                                      long tacoId){
        jdbc.update(
                "insert into Taco_Ingredients (taco, ingredient) values (?,?)",
                tacoId, ingredient.getId()
        );
    }
}
