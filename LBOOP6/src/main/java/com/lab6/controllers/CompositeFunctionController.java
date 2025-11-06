package com.lab6.controllers;

import com.lab6.dto.CompositeFunctionRequest;
import com.lab6.dto.CompositeFunctionResponse;
import com.lab6.entity.CompositeFunction;
import com.lab6.entity.User;
import com.lab6.repository.CompositeFunctionRepository;
import com.lab6.repository.UserRepository;
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
@RequestMapping("/spring/composite-functions")
@RequiredArgsConstructor
public class CompositeFunctionController {
    private final CompositeFunctionRepository compositeFunctionRepository;
    private final UserRepository userRepository;

    private CompositeFunctionResponse toResponse(CompositeFunction compositeFunction) {
        return new CompositeFunctionResponse(compositeFunction.getId(), compositeFunction.getName(), compositeFunction.getOwner().getId());
    }

    private CompositeFunction toEntity(CompositeFunctionRequest request, User owner) {
        CompositeFunction compositeFunction = new CompositeFunction();
        compositeFunction.setName(request.getName());
        compositeFunction.setOwner(owner);

        return compositeFunction;
    }

    @GetMapping
    public ResponseEntity<?> getCompositeFunctions(@RequestParam(required = false) Long id, @RequestParam(required = false) Long ownerId) {
        if (id != null)
            return compositeFunctionRepository.findById(id).map(this::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

        else if (ownerId != null) {
            List<CompositeFunction> compositeFunctions = compositeFunctionRepository.findByOwnerId(ownerId);
            List<CompositeFunctionResponse> responses = compositeFunctions.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }

        else
            return ResponseEntity.badRequest().body("Укажите параметр id или ownerId");
    }

    @PostMapping
    public ResponseEntity<CompositeFunctionResponse> create(@RequestBody CompositeFunctionRequest request) {
        log.info("Create CompositeFunction: {}", request);
        User owner = userRepository.findById(request.getOwnerId()).orElse(null);
        if (owner == null) {
            log.warn("Owner with id {} not found", request.getOwnerId());
            return ResponseEntity.badRequest().build();
        }

        CompositeFunction compositeFunction = toEntity(request, owner);
        CompositeFunction saved = compositeFunctionRepository.save(compositeFunction);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompositeFunctionResponse> update(@PathVariable Long id, @RequestBody CompositeFunctionRequest request) {
        log.info("Update CompositeFunction id: {}, data: {}", id, request);
        Optional<CompositeFunction> compositeFunctionOptional = compositeFunctionRepository.findById(id);
        if (compositeFunctionOptional.isEmpty())
            return ResponseEntity.<CompositeFunctionResponse>notFound().build();

        User owner = userRepository.findById(request.getOwnerId()).orElse(null);

        if (owner == null) {
            log.warn("Owner with id {} not found", request.getOwnerId());
            return ResponseEntity.<CompositeFunctionResponse>badRequest().build();
        }

        CompositeFunction existing = compositeFunctionOptional.get();
        existing.setName(request.getName());
        existing.setOwner(owner);
        compositeFunctionRepository.save(existing);

        return ResponseEntity.ok(toResponse(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete CompositeFunction id: {}", id);
        if (compositeFunctionRepository.existsById(id)) {
            compositeFunctionRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}