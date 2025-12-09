package mk.ukim.finki.wp.lab.web.controller;

import mk.ukim.finki.wp.lab.model.Chef;
import mk.ukim.finki.wp.lab.model.Dish;
import mk.ukim.finki.wp.lab.service.ChefService;
import mk.ukim.finki.wp.lab.service.DishService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/dishes")
public class DishController {

    private final DishService dishService;
    private final ChefService chefService;

    public DishController(DishService dishService, ChefService chefService) {
        this.dishService = dishService;
        this.chefService = chefService;
    }

    // Прикажи сите јадења
    @GetMapping
    public String getDishesPage(@RequestParam(required = false) String error, Model model) {
        if (error != null && !error.isEmpty()) {
            model.addAttribute("hasError", true);
            model.addAttribute("error", error);
        }
        List<Dish> dishes = dishService.listDishes();
        model.addAttribute("dishes_list", dishes);
        return "listDishes";
    }

    // Форма за додавање ново јадење
    @GetMapping("/dish-form")
    public String getAddDishPage(Model model) {
        model.addAttribute("dish", null); // нема уште јадење
        model.addAttribute("formAction", "/dishes/add");

        // ОВА Е КЛУЧНО: додај ги готвачите во моделот
        List<Chef> chefs = chefService.listChefs();
        model.addAttribute("chefs", chefs);

        return "dish-form";
    }

    // Форма за уредување на постоечко јадење
    @GetMapping("/dish-form/{id}")
    public String getEditDishForm(@PathVariable Long id, Model model) {
        Optional<Dish> dish = dishService.findById(id);
        if (dish.isEmpty()) {
            return "redirect:/dishes?error=DishNotFound";
        }
        model.addAttribute("dish", dish.get());
        model.addAttribute("formAction", "/dishes/edit/" + id);

        // ОВДЕ СЕ ДОДАВААТ Готвачите
        List<Chef> chefs = chefService.listChefs();
        model.addAttribute("chefs", chefs);

        return "dish-form";
    }

    // Додавање ново јадење
    @PostMapping("/add")
    public String saveDish(@RequestParam String dishId,
                           @RequestParam String name,
                           @RequestParam String cuisine,
                           @RequestParam int preparationTime,
                           @RequestParam Long chefId) {

        Chef chef = chefService.findById(chefId);
        if (chef != null) {
            dishService.create(dishId, name, cuisine, preparationTime, chef);
        }
        return "redirect:/dishes";
    }

    // Ажурирање на постоечко јадење
    @PostMapping("/edit/{id}")
    public String editDish(@PathVariable Long id,
                           @RequestParam String dishId,
                           @RequestParam String name,
                           @RequestParam String cuisine,
                           @RequestParam int preparationTime,
                           @RequestParam Long chefId) {

        Chef chef = chefService.findById(chefId);
        if (chef != null) {
            dishService.update(id, dishId, name, cuisine, preparationTime, chef);
        }
        return "redirect:/dishes";
    }

    // Бришење јадење
    @PostMapping("/delete/{id}")
    public String deleteDish(@PathVariable Long id) {
        dishService.delete(id);
        return "redirect:/dishes";
    }

    // Прикажи јадења за конкретен готвач
    @GetMapping("/chef/{chefId}")
    public String listDishesByChef(@PathVariable Long chefId, Model model) {
        Chef chef = chefService.findById(chefId);
        if (chef == null) {
            return "redirect:/dishes?error=ChefNotFound";
        }
        model.addAttribute("chef", chef);
        model.addAttribute("dishes_list", chef.getDishes());
        return "listDishes";
    }


    // Додавање јадење за конкретен готвач
    @PostMapping("/add/{chefId}")
    public String addDishToChef(@PathVariable Long chefId,
                                @RequestParam String dishId,
                                @RequestParam String name,
                                @RequestParam String cuisine,
                                @RequestParam int preparationTime) {

        Chef chef = chefService.findById(chefId);
        if (chef != null) {
            dishService.create(dishId, name, cuisine, preparationTime, chef);
        }
        return "redirect:/dishes/chef/" + chefId;
    }
    @GetMapping("/chefs")
    public String listChefs(Model model) {
        model.addAttribute("chefs", chefService.listChefs());
        return "listChefs"; // Thymeleaf шаблон со листа на готвачи
    }

}
