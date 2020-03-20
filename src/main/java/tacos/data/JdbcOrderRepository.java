package tacos.data;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import tacos.Taco;
import tacos.Order;

@Repository
public class JdbcOrderRepository implements OrderRepository{
    private SimpleJdbcInsert orderInserter;
    private SimpleJdbcInsert orderTacoInserter;
    private ObjectMapper objectMapper;
    private JdbcTemplate jdbc;

    @Autowired
    public JdbcOrderRepository(JdbcTemplate jdbc){
        this.orderInserter = new SimpleJdbcInsert(jdbc)
                .withTableName("Taco_Order")
                .usingGeneratedKeyColumns("id");
        this.orderTacoInserter = new SimpleJdbcInsert(jdbc)
                .withTableName("Taco_Order_Tacos");
        this.objectMapper = new ObjectMapper();
        this.jdbc = jdbc;
    }

    @Override
    public Iterable<Order> findAll(){
        return jdbc.query("SELECT * FROM Taco_Order", this::mapRowToOrder);
    }

    private Order mapRowToOrder(ResultSet rs, int rowNum)
        throws SQLException {
            Order order = new Order();
            order.setId(rs.getLong("id"));
            order.setPlacedAt(rs.getDate("placedAt"));
            order.setName(rs.getString("name"));
            order.setStreet(rs.getString("street"));
            order.setCity(rs.getString("city"));
            order.setState(rs.getString("state"));
            order.setZip(rs.getString("zip"));
            order.setCcNumber(rs.getString("ccNumber"));
            order.setCcExpiration(rs.getString("ccExpiration"));
            order.setCcCVV(rs.getString("ccCVV"));
           return order;
        }

    @Override
    public Order save(Order order) {
        order.setPlacedAt(new Date());
        long orderId = saveOrderDetails(order);
        order.setId(orderId);
        List<Taco> tacos = order.getTacos();
        for (Taco taco : tacos) {
            saveTacoToOrder(taco, orderId);
        }
        return order;
    }

    private long saveOrderDetails(Order order) {
        @SuppressWarnings("unchecked")
        Map<String, Object> values =
                objectMapper.convertValue(order, Map.class);
        values.put("placedAt", order.getPlacedAt());

        long orderId =
                orderInserter
                        .executeAndReturnKey(values)
                        .longValue();
        return orderId;
    }

    private void saveTacoToOrder(Taco taco, long orderId) {
        Map<String, Object> values = new HashMap<>();
        values.put("tacoOrder", orderId);
        values.put("taco", taco.getId());
        orderTacoInserter.execute(values);
    }
}
