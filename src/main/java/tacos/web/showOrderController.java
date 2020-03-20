package tacos.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import tacos.Order;
import tacos.data.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/showOrders")
public class showOrderController {

    private OrderRepository orderRepository;

    @Autowired
    public showOrderController(OrderRepository orderRepository){
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String showOrders(Model model){
        // Get orders from database
        List<Order> orders = new ArrayList<>();
        orderRepository.findAll().forEach(i -> orders.add(i));
        model.addAttribute("orders", orders);
        return "showOrders";
    }
}
