package ru.ssau.tk.samsa.lb6.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.ssau.tk.samsa.lb6.model.Function;
import ru.ssau.tk.samsa.lb6.service.FunctionService;

import java.util.List;

@RestController
@RequestMapping("/api/functions")
public class FunctionController {
    private final FunctionService functionService;

    public FunctionController(FunctionService functionService) {
        this.functionService = functionService;
    }

    @PostMapping
    public ResponseEntity<Function> create(@RequestBody Function f) {
        Function saved = functionService.create(f);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<Function>> getByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(functionService.findByOwner(ownerId));
    }
}
