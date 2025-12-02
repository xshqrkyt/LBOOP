package com.lab7.mapper;

import com.lab7.dto.*;
import com.lab7.entity.*;
import com.lab7.enums.FunctionType;
import com.lab7.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Arrays;

public class Mapper {
    // User
    public static User toEntity(UserRequest request) {
        if (request == null)
            return null;

        return new User(null, request.getUsername(), request.getPasswordHash(), request.getEmail(), request.getRole(), LocalDateTime.now());
    }

    public static UserResponse toResponse(User user) {
        if (user == null)
            return null;

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }

    // Function
    public static Function toEntity(FunctionRequest request) {
        if (request == null)
            return null;

        return new Function(null, request.getName(), FunctionType.valueOf(request.getType().toUpperCase()), LocalDateTime.now(), request.getOwnerId());
    }

    public static FunctionResponse toResponse(Function function) {
        if (function == null)
            return null;

        return new FunctionResponse(function.getId(), function.getName(), function.getType(), function.getCreatedAt(), function.getOwnerId());
    }

    // Points
    public static Points toEntity(PointsRequest request) {
        if (request == null)
            return null;

        Double[] xValuesCopy = (request.getXValues() != null) ? Arrays.copyOf(request.getXValues(), request.getXValues().length) : new Double[0];
        Double[] yValuesCopy = (request.getYValues() != null) ? Arrays.copyOf(request.getYValues(), request.getYValues().length) : new Double[0];

        return new Points(null, xValuesCopy, yValuesCopy, request.getFunctionId());
    }

    public static PointsResponse toResponse(Points points) {
        if (points == null)
            return null;

        Double[] xValuesCopy = (points.getXValues() != null) ? Arrays.copyOf(points.getXValues(), points.getXValues().length) : new Double[0];
        Double[] yValuesCopy = (points.getYValues() != null) ? Arrays.copyOf(points.getYValues(), points.getYValues().length) : new Double[0];

        return new PointsResponse(points.getId(), xValuesCopy, yValuesCopy, points.getFunctionId());
    }

    // CompositeFunction
    public static CompositeFunction toEntity(CompositeFunctionRequest request) {
        if (request == null)
            return null;

        return new CompositeFunction(null, request.getName(), null, request.getOwnerId());
    }

    public static CompositeFunctionResponse toResponse(CompositeFunction compositeFunction) {
        if (compositeFunction == null)
            return null;

        return new CompositeFunctionResponse(compositeFunction.getId(), compositeFunction.getName(), compositeFunction.getCreatedAt(), compositeFunction.getOwnerId());
    }

    // CompositeFunctionLink
    public static CompositeFunctionLink toEntity(CompositeFunctionLinkRequest request) {
        if (request == null)
            return null;

        return new CompositeFunctionLink(null, request.getCompositeId(), request.getFunctionId(), request.getOrderIndex());
    }

    public static CompositeFunctionLinkResponse toResponse(CompositeFunctionLink compositeFunctionLink) {
        if (compositeFunctionLink == null)
            return null;

        return new CompositeFunctionLinkResponse(compositeFunctionLink.getId(), compositeFunctionLink.getCompositeId(), compositeFunctionLink.getFunctionId(), compositeFunctionLink.getOrderIndex());
    }
}