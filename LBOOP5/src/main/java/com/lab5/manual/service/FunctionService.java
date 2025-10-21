package com.lab5.manual.service;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.FunctionResponse;
import com.lab5.common.dto.PointRequest;
import com.lab5.common.dto.PointResponse;
import com.lab5.manual.entity.Function;
import com.lab5.manual.entity.Point;
import com.lab5.manual.repository.FunctionRepository;
import com.lab5.manual.repository.PointRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);
    private final FunctionRepository functionRepository;
    private final PointRepository pointRepository;

    public FunctionService(FunctionRepository functionRepository, PointRepository pointRepository) {
        this.functionRepository = functionRepository;
        this.pointRepository = pointRepository;
    }

    public FunctionResponse createFunction(FunctionRequest request, Long ownerId) {
        logger.info("Creating function for owner: {}", ownerId);

        // Create function
        Function function = new Function();
        function.setName(request.getName());
        function.setType(request.getType());
        function.setOwnerId(ownerId);

        Long functionId = functionRepository.create(function);
        function.setId(functionId);

        // Create points
        if (request.getPoints() != null) {
            for (PointRequest pointRequest : request.getPoints()) {
                Point point = new Point();
                point.setXValue(pointRequest.getX());
                point.setYValue(pointRequest.getY());
                point.setFunctionId(functionId);
                pointRepository.create(point);
            }
        }

        return toFunctionResponse(function);
    }

    public List<FunctionResponse> getUserFunctions(Long ownerId) {
        logger.debug("Getting functions for user: {}", ownerId);
        List<Function> functions = functionRepository.findByOwnerId(ownerId);

        return functions.stream()
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    public List<FunctionResponse> searchFunctions(String name, Long ownerId) {
        logger.debug("Searching functions with name: {} for user: {}", name, ownerId);
        List<Function> functions = functionRepository.findByNameContaining(name);

        return functions.stream()
                .filter(f -> f.getOwnerId().equals(ownerId))
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    private FunctionResponse toFunctionResponse(Function function) {
        FunctionResponse response = new FunctionResponse();
        response.setId(function.getId());
        response.setName(function.getName());
        response.setType(function.getType());
        response.setCreatedAt(function.getCreatedAt());
        response.setOwnerId(function.getOwnerId());

        // Get points for this function
        List<Point> points = pointRepository.findByFunctionId(function.getId());
        List<PointResponse> pointResponses = points.stream()
                .map(this::toPointResponse)
                .collect(Collectors.toList());
        response.setPoints(pointResponses);

        return response;
    }

    private PointResponse toPointResponse(Point point) {
        PointResponse response = new PointResponse();
        response.setId(point.getId());
        response.setX(point.getXValue());
        response.setY(point.getYValue());
        return response;
    }
}
