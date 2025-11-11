package com.lab7.controllers;

import com.lab7.dto.FunctionRequest;
import com.lab7.dto.FunctionResponse;
import com.lab7.entity.Function;
import com.lab7.entity.User;
import com.lab7.repository.FunctionRepository;
import com.lab7.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> getFunctions(@RequestParam(required = false) Long id, @RequestParam(required = false) Long ownerId) {
        if (id != null)
            return functionRepository.findById(id).map(f -> ResponseEntity.ok(toResponse(f))).orElse(ResponseEntity.notFound().build());

        else if (ownerId != null) {
            List<Function> functions = functionRepository.findByOwnerId(ownerId);
            List<FunctionResponse> responses = functions.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }

        else
            return ResponseEntity.badRequest().body("Нужно указать параметр 'id' или 'ownerId'");
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

    @PutMapping("/{id}")
    public ResponseEntity<FunctionResponse> update(@PathVariable Long id, @RequestBody FunctionRequest request) {
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


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete Function id: {}", id);
        if (functionRepository.existsById(id)) {
            functionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}