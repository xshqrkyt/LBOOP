package com.lab6.controllers;

import com.lab6.dto.CompositeFunctionLinkRequest;
import com.lab6.dto.CompositeFunctionLinkResponse;
import com.lab6.entity.CompositeFunction;
import com.lab6.entity.CompositeFunctionLink;
import com.lab6.entity.Function;
import com.lab6.repository.CompositeFunctionLinkRepository;
import com.lab6.repository.CompositeFunctionRepository;
import com.lab6.repository.FunctionRepository;
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
@RequestMapping("/spring/composite-function-links")
@RequiredArgsConstructor
public class CompositeFunctionLinkController {

    private final CompositeFunctionLinkRepository compositeFunctionLinkRepository;
    private final CompositeFunctionRepository compositeFunctionRepository;
    private final FunctionRepository functionRepository;

    private CompositeFunctionLinkResponse toResponse(CompositeFunctionLink link) {
        return new CompositeFunctionLinkResponse(link.getId(), link.getCompositeFunction().getId(), link.getFunction().getId(), link.getOrderIndex());
    }

    private CompositeFunctionLink toEntity(CompositeFunctionLinkRequest request, CompositeFunction compositeFunction, Function function) {
        CompositeFunctionLink link = new CompositeFunctionLink();
        link.setCompositeFunction(compositeFunction);
        link.setFunction(function);
        link.setOrderIndex(request.getOrderIndex());

        return link;
    }

    @GetMapping
    public ResponseEntity<?> getCompositeFunctionLinks(@RequestParam(required = false) Long id, @RequestParam(required = false) Long compositeId, @RequestParam(required = false) Long functionId) {
        if (id != null)
            return compositeFunctionLinkRepository.findById(id).map(this::toResponse).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());

        else if (compositeId != null) {
            List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByCompositeFunctionId(compositeId);
            List<CompositeFunctionLinkResponse> responses = links.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }

        else if (functionId != null) {
            List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByFunctionId(functionId);
            List<CompositeFunctionLinkResponse> responses = links.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }

        else
            return ResponseEntity.badRequest().body("Укажите параметр id, compositeId или functionId");
    }

    @PostMapping
    public ResponseEntity<CompositeFunctionLinkResponse> create(@RequestBody CompositeFunctionLinkRequest request) {
        log.info("Create CompositeFunctionLink: {}", request);
        CompositeFunction compositeFunction = compositeFunctionRepository.findById(request.getCompositeId()).orElse(null);
        if (compositeFunction == null) {
            log.warn("CompositeFunction with id {} not found", request.getCompositeId());
            return ResponseEntity.<CompositeFunctionLinkResponse>badRequest().build();
        }

        Function function = functionRepository.findById(request.getFunctionId()).orElse(null);
        if (function == null) {
            log.warn("Function with id {} not found", request.getFunctionId());
            return ResponseEntity.<CompositeFunctionLinkResponse>badRequest().build();
        }

        CompositeFunctionLink link = toEntity(request, compositeFunction, function);
        CompositeFunctionLink saved = compositeFunctionLinkRepository.save(link);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompositeFunctionLinkResponse> update(@PathVariable Long id, @RequestBody CompositeFunctionLinkRequest request) {
        log.info("Update CompositeFunctionLink id: {}, data: {}", id, request);

        Optional<CompositeFunctionLink> optionalLink = compositeFunctionLinkRepository.findById(id);
        if (optionalLink.isEmpty())
            return ResponseEntity.<CompositeFunctionLinkResponse>notFound().build();

        CompositeFunctionLink existing = optionalLink.get();

        CompositeFunction compositeFunction = compositeFunctionRepository.findById(request.getCompositeId()).orElse(null);
        if (compositeFunction == null) {
            log.warn("CompositeFunction with id {} not found", request.getCompositeId());
            return ResponseEntity.<CompositeFunctionLinkResponse>badRequest().build();
        }

        Function function = functionRepository.findById(request.getFunctionId()).orElse(null);
        if (function == null) {
            log.warn("Function with id {} not found", request.getFunctionId());
            return ResponseEntity.<CompositeFunctionLinkResponse>badRequest().build();
        }

        existing.setCompositeFunction(compositeFunction);
        existing.setFunction(function);
        existing.setOrderIndex(request.getOrderIndex());

        compositeFunctionLinkRepository.save(existing);

        return ResponseEntity.ok(toResponse(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete CompositeFunctionLink id: {}", id);
        if (compositeFunctionLinkRepository.existsById(id)) {
            compositeFunctionLinkRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}