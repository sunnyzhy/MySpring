package org.example.controller;

import org.example.model.A;
import org.example.service.AService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/a")
public class AController {
    private final AService service;

    public AController(AService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public List<A> get(@PathVariable int id){
        return service.get(id);
    }

    @PostMapping()
    public void del(){
        service.del();
    }
}
