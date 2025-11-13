package com.lab7.controllers;

import com.lab7.dto.*;
import com.lab7.entity.*;
import com.lab7.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/lab6-1.0-SNAPSHOT/points")
@RequiredArgsConstructor
public class PointController {

    private final PointRepository pointRepository;
    private final FunctionRepository functionRepository;

    private PointResponse toResponse(Point point) {
        return new PointResponse(point.getId(), point.getXValue(), point.getYValue(), point.getFunction().getId());
    }

    private Point toEntity(PointRequest request, Function function) {
        Point point = new Point();
        point.setXValue(request.getX());
        point.setYValue(request.getY());
        point.setFunction(function);
        return point;
    }

    @GetMapping
    public ResponseEntity<?> getPoints(@RequestParam(required = false) Long id) {
        Point point = pointRepository.findByFunctionId(id);
        if (point == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(toResponse(point));
    }

    @PostMapping
    public ResponseEntity<PointResponse> create(@RequestBody PointRequest request) {
        log.info("Create Point: {}", request);
        Function function = functionRepository.findById(request.getFunctionId()).orElse(null);
        if (function == null) {
            log.warn("Function with id {} not found", request.getFunctionId());
            return ResponseEntity.badRequest().build();
        }

        Point point = toEntity(request, function);
        Point saved = pointRepository.save(point);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping
    public ResponseEntity<PointResponse> update(@RequestParam Long id, @RequestBody PointRequest request) {
        log.info("Update Point id: {}, data: {}", id, request);
        Point existing = pointRepository.findByFunctionId(id);

        if (existing == null)
            return ResponseEntity.<PointResponse>notFound().build();

        Function function = functionRepository.findById(request.getFunctionId()).orElse(null);
        if (function == null) {
            log.warn("Function with id {} not found", request.getFunctionId());
            return ResponseEntity.<PointResponse>badRequest().build();
        }

        existing.setXValue(request.getX());
        existing.setYValue(request.getY());
        existing.setFunction(function);
        pointRepository.save(existing);

        return ResponseEntity.ok(toResponse(existing));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteByFunctionId(@RequestParam Long id) {
        log.info("Delete Points by functionId: {}", id);

        Point point = pointRepository.findByFunctionId(id);
        if (point == null)
            return ResponseEntity.notFound().build();

        pointRepository.deleteById(point.getId());

        return ResponseEntity.noContent().build();
    }
}