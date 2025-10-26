package com.lab5.mapper;

import com.lab5.enums.FunctionType;
import com.lab5.enums.UserRole;
import com.lab5.dto.*;
import com.lab5.entity.*;

import java.time.LocalDateTime;

public class Mapper {
    // User
    public static User toEntity(UserRequest request) {
        if (request == null)
            return null;

        return new User(null, request.getUsername(), null, request.getEmail(), UserRole.valueOf(request.getRole()), LocalDateTime.now());
    }

    public static UserResponse toResponse(User user) {
        if (user == null)
            return null;

        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole().name());
    }

    // Function
    public static Function toEntity(FunctionRequest request) {
        if (request == null)
            return null;

        return new Function(null, request.getName(), FunctionType.valueOf(request.getType()), LocalDateTime.now(), request.getOwnerId());
    }

    public static FunctionResponse toResponse(Function function) {
        if (function == null)
            return null;

        return new FunctionResponse(function.getId(), function.getName(), function.getType().name(), function.getOwnerId());
    }

    // Points
    public static Points toEntity(PointsRequest request) {
        if (request == null)
            return null;

        return new Points(null, request.getXValues(), request.getYValues(), request.getFunctionId());
    }

    public static PointsResponse toResponse(Points points) {
        if (points == null)
            return null;

        return new PointsResponse(points.getId(), points.getXValues(), points.getYValues(), points.getFunctionId());
    }

    // CompositeFunction
    public static CompositeFunction toEntity(CompositeFunctionRequest request) {
        if (request == null)
            return null;

        return new CompositeFunction(null, request.getName(), request.getOwnerId(), null);
    }

    public static CompositeFunctionResponse toResponse(CompositeFunction compositeFunction) {
        if (compositeFunction == null)
            return null;

        return new CompositeFunctionResponse(compositeFunction.getId(), compositeFunction.getName(), compositeFunction.getOwnerId());
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