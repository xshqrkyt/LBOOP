package com.lab5.framework.service;

import com.lab5.common.dto.FunctionRequest;
import com.lab5.common.dto.FunctionResponse;
import com.lab5.common.dto.PointRequest;
import com.lab5.common.dto.PointResponse;
import com.lab5.framework.entity.Function;
import com.lab5.framework.entity.Point;
import com.lab5.framework.entity.User;
import com.lab5.framework.repository.FunctionRepository;
import com.lab5.framework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FunctionService {
    private static final Logger logger = LoggerFactory.getLogger(FunctionService.class);
    private final FunctionRepository functionRepository;
    private final UserRepository userRepository;

    public FunctionService(FunctionRepository functionRepository, UserRepository userRepository) {
        this.functionRepository = functionRepository;
        this.userRepository = userRepository;
    }

    public FunctionResponse createFunction(FunctionRequest request, Long ownerId) {
        logger.info("Creating function for owner: {}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Function function = new Function();
        function.setName(request.getName());
        function.setType(request.getType());
        function.setOwner(owner);

        // Create points
        if (request.getPoints() != null) {
            for (PointRequest pointRequest : request.getPoints()) {
                Point point = new Point();
                point.setXValue(pointRequest.getX());
                point.setYValue(pointRequest.getY());
                point.setFunction(function);
                function.getPoints().add(point);
            }
        }

        Function saved = functionRepository.save(function);
        logger.info("Created function with id: {}", saved.getId());

        return toFunctionResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FunctionResponse> getUserFunctions(Long ownerId) {
        logger.debug("Getting functions for user: {}", ownerId);
        List<Function> functions = functionRepository.findByOwnerIdWithPoints(ownerId);

        return functions.stream()
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FunctionResponse> searchFunctions(String name, Long ownerId) {
        logger.debug("Searching functions with name: {} for user: {}", name, ownerId);
        List<Function> functions = functionRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, name);

        return functions.stream()
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FunctionResponse> getUserFunctionsSortedByName(Long ownerId) {
        logger.debug("Getting functions sorted by name for user: {}", ownerId);
        List<Function> functions = functionRepository.findByOwnerIdOrderByNameAsc(ownerId);

        return functions.stream()
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FunctionResponse> getUserFunctionsSortedByDate(Long ownerId) {
        logger.debug("Getting functions sorted by date for user: {}", ownerId);
        List<Function> functions = functionRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);

        return functions.stream()
                .map(this::toFunctionResponse)
                .collect(Collectors.toList());
    }

    private FunctionResponse toFunctionResponse(Function function) {
        FunctionResponse response = new FunctionResponse();
        response.setId(function.getId());
        response.setName(function.getName());
        response.setType(function.getType());
        response.setCreatedAt(function.getCreatedAt());
        response.setOwnerId(function.getOwner().getId());

        List<PointResponse> pointResponses = function.getPoints().stream()
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
