package com.lab6.controllers;

import com.lab6.dto.PointRequest;
import com.lab6.dto.PointResponse;
import com.lab6.entity.Function;
import com.lab6.entity.Point;
import com.lab6.repository.FunctionRepository;
import com.lab6.repository.PointRepository;
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
@RequestMapping("/spring/points")
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
    public ResponseEntity<?> getPoints(@RequestParam(required = false) Long id, @RequestParam(required = false) Long functionId) {
        if (id != null)
            return pointRepository.findById(id).map(p -> ResponseEntity.ok(toResponse(p))).orElse(ResponseEntity.notFound().build());

        else if (functionId != null) {
            List<Point> points = pointRepository.findByFunctionId(functionId);
            List<PointResponse> responses = points.stream().map(this::toResponse).collect(Collectors.toList());
            return ResponseEntity.ok(responses);
        }

        else
            return ResponseEntity.badRequest().body("Необходимо указать параметр 'id' или 'functionId'");
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

    @PutMapping("/{id}")
    public ResponseEntity<PointResponse> update(@PathVariable Long id, @RequestBody PointRequest request) {
        log.info("Update Point id: {}, data: {}", id, request);
        Optional<Point> optionalPoint = pointRepository.findById(id);

        if (optionalPoint.isEmpty())
            return ResponseEntity.<PointResponse>notFound().build();

        Function function = functionRepository.findById(request.getFunctionId()).orElse(null);
        if (function == null) {
            log.warn("Function with id {} not found", request.getFunctionId());
            return ResponseEntity.<PointResponse>badRequest().build();
        }

        Point existing = optionalPoint.get();
        existing.setXValue(request.getX());
        existing.setYValue(request.getY());
        existing.setFunction(function);
        pointRepository.save(existing);

        return ResponseEntity.ok(toResponse(existing));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Delete Point id: {}", id);
        if (pointRepository.existsById(id)) {
            pointRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}