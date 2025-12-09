package mk.ukim.finki.wp.lab.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Getter
@Setter
@Table(name = "chef")
public class Chef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "chef", cascade = CascadeType.ALL)
    private List<Dish> dishes = new ArrayList<>();
    public Chef() {
    }
    public Chef(String name) {
        this.name = name;
    }

    public Chef(String name, List<Dish> dishes) {
        this.name = name;
        this.dishes = dishes;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Dish> getDishes() {
        return dishes;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDishes(List<Dish> dishes) {
        this.dishes = dishes;
    }
}
