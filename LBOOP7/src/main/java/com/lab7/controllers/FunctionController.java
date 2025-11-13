package com.lab7.controllers;

import com.lab7.dto.*;
import com.lab7.entity.*;
import com.lab7.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/lab6-1.0-SNAPSHOT/functions")
@RequiredArgsConstructor
public class FunctionController {
    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;

    private FunctionResponse toResponse(Function function) {
        return new FunctionResponse(function.getId(), function.getName(), function.getType(), Long.valueOf(function.getOwner().getId()));
    }

    private Function toEntity(FunctionRequest request, User owner) {
        Function function = new Function();
        function.setName(request.getName());
        function.setType(request.getType());
        function.setOwner(owner);

        return function;
    }

    @GetMapping
    public ResponseEntity<?> getFunctions(@RequestParam(required = false) Long id, @RequestParam(required = false) Long ownerId, @RequestParam(required = false) String name, @RequestParam(required = false) String type) {
        if (id != null)
            return functionRepository.findById(id).map(f -> ResponseEntity.ok(toResponse(f))).orElse(ResponseEntity.notFound().build());

        else if (ownerId != null) {
            List<Function> functions = functionRepository.findByOwnerId(ownerId);
            return ResponseEntity.ok(functions.stream().map(this::toResponse).toList());
        }

        else if (name != null) {
            List<Function> functions = functionRepository.findByName(name);
            return ResponseEntity.ok(functions.stream().map(this::toResponse).toList());
        }

        else if (type != null) {
            List<Function> functions = functionRepository.findByType(type);
            return ResponseEntity.ok(functions.stream().map(this::toResponse).toList());
        }

        return ResponseEntity.badRequest().body("Нужно указать параметр 'id', 'ownerId', 'name' или 'type'");
    }

    @PostMapping
    public ResponseEntity<FunctionResponse> create(@RequestBody FunctionRequest request) {
        log.info("Create Function: {}", request);
        User owner = userRepository.findById(Long.valueOf(request.getOwnerId())).orElse(null);
        if (owner == null) {
            log.warn("Owner with id {} not found", request.getOwnerId());
            return ResponseEntity.<FunctionResponse>badRequest().build();
        }

        Function function = toEntity(request, owner);
        Function saved = functionRepository.save(function);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping
    public ResponseEntity<FunctionResponse> update(@RequestParam Long id, @RequestBody FunctionRequest request) {
        log.info("Update Function id: {}, data: {}", id, request);
        Optional<Function> functionOptional = functionRepository.findById(id);
        if (functionOptional.isEmpty())
            return ResponseEntity.notFound().build();

        User owner = userRepository.findById(Long.valueOf(request.getOwnerId())).orElse(null);
        if (owner == null) {
            log.warn("Owner with id {} not found", request.getOwnerId());
            return ResponseEntity.badRequest().build();
        }

        Function existing = functionOptional.get();
        existing.setName(request.getName());
        existing.setType(request.getType());
        existing.setOwner(owner);
        functionRepository.save(existing);

        log.info("Function has saved.");
        return ResponseEntity.ok(toResponse(existing));
    }


    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestParam Long id) {
        log.info("Delete Function id: {}", id);
        if (functionRepository.existsById(id)) {
            functionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}