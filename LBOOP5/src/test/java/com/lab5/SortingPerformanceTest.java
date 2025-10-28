package com.lab5;

import com.lab5.common.enums.FunctionType;
import com.lab5.common.enums.UserRole;
import com.lab5.entity.*;
import com.lab5.repository.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class SortingPerformanceTest {

    @Autowired private UserRepository userRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private CompositeFunctionRepository compositeFunctionRepository;
    @Autowired private CompositeFunctionLinkRepository compositeFunctionLinkRepository;

    private final int RECORDS = 10000;
    private final Random random = new Random();

    private List<User> createdUsers;
    private List<Function> allFunctions;
    private List<CompositeFunction> allCompositeFunctions;
    private List<Function> functionsForLinks;

    private String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private FunctionType randomFunctionType() {
        return FunctionType.values()[random.nextInt(FunctionType.values().length)];
    }

    @BeforeEach
    public void setup() {
        createdUsers = new ArrayList<>();
        allFunctions = new ArrayList<>();
        allCompositeFunctions = new ArrayList<>();
        functionsForLinks = new ArrayList<>();
    }

    @Test
    public void runSortingPerformanceTests() throws IOException {
        StringBuilder csv = new StringBuilder("repository,query,duration (ms)\n");

        // Создаем пользователей и связанные данные (без замера)
        for (int i = 0; i < RECORDS; i++) {
            User user = new User();
            user.setUsername("user_" + randomString());
            user.setEmail("user" + i + "@example.com");
            user.setPasswordHash(randomString());
            user.setRole(UserRole.USER);
            user = userRepository.save(user);
            createdUsers.add(user);

            Function func = new Function();
            func.setName("Function_" + randomString());
            func.setType(randomFunctionType().name());
            func.setOwner(user);
            func = functionRepository.save(func);
            allFunctions.add(func);

            CompositeFunction compositeFunction = new CompositeFunction();
            compositeFunction.setName("CompFunction_" + randomString());
            compositeFunction.setOwner(user);
            compositeFunction = compositeFunctionRepository.save(compositeFunction);
            allCompositeFunctions.add(compositeFunction);

            functionsForLinks.add(func);

            if (i % 1000 == 0) {
                userRepository.flush();
                functionRepository.flush();
                compositeFunctionRepository.flush();
            }
        }

        userRepository.flush();
        functionRepository.flush();
        compositeFunctionRepository.flush();

        // Создаем links для composite функций без темпа, важна сортировка
        for (int i = 0; i < RECORDS; i++) {
            CompositeFunctionLink link = new CompositeFunctionLink();
            link.setCompositeFunction(allCompositeFunctions.get(i));
            link.setFunction(functionsForLinks.get(random.nextInt(functionsForLinks.size())));
            link.setOrderIndex(i);
            compositeFunctionLinkRepository.save(link);

            if (i % 1000 == 0)
                compositeFunctionLinkRepository.flush();
        }

        compositeFunctionLinkRepository.flush();

        long start, duration;

        // UserRepository сортированные поиски
        Sort sortUsernameAsc = Sort.by(Sort.Direction.ASC, "username");
        start = System.currentTimeMillis();
        userRepository.findAll(sortUsernameAsc);
        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,findAllSortedByUsernameAsc,").append(duration).append("\n");

        Sort sortEmailDesc = Sort.by(Sort.Direction.DESC, "email");
        start = System.currentTimeMillis();
        userRepository.findAll(sortEmailDesc);
        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,findAllSortedByEmailDesc,").append(duration).append("\n");

        // FunctionRepository сортированные поиски
        if (!createdUsers.isEmpty()) {
            Long ownerId = createdUsers.get(0).getId();

            start = System.currentTimeMillis();
            functionRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId);
            duration = System.currentTimeMillis() - start;
            csv.append("FunctionRepository,findByOwnerIdOrderByCreatedAtDesc,").append(duration).append("\n");

            start = System.currentTimeMillis();
            functionRepository.findByOwnerIdOrderByNameAsc(ownerId);
            duration = System.currentTimeMillis() - start;
            csv.append("FunctionRepository,findByOwnerIdOrderByNameAsc,").append(duration).append("\n");
        }

        // PointRepository сортированный findByFunctionId
        if (!allFunctions.isEmpty()) {
            Long functionId = allFunctions.get(0).getId();

            // С сортировкой по xValue ASC
            Sort sortXAsc = Sort.by(Sort.Direction.ASC, "xValue");
            start = System.currentTimeMillis();
            pointRepository.findByFunctionId(functionId, sortXAsc);
            duration = System.currentTimeMillis() - start;
            csv.append("PointRepository,findByFunctionIdSortedByXAsc,").append(duration).append("\n");
        }

        // CompositeFunctionRepository с сортировкой через fetch join
        if (!createdUsers.isEmpty()) {
            Long ownerId = createdUsers.get(0).getId();
            Sort sortNameAsc = Sort.by(Sort.Direction.ASC, "name");

            start = System.currentTimeMillis();
            compositeFunctionRepository.findByOwnerIdWithLinks(ownerId, sortNameAsc);
            duration = System.currentTimeMillis() - start;
            csv.append("CompositeFunctionRepository,findByOwnerIdWithLinksSortedByNameAsc,").append(duration).append("\n");
        }

        // CompositeFunctionLinkRepository сортированные методы
        if (!allCompositeFunctions.isEmpty()) {
            Long compositeFunctionId = allCompositeFunctions.get(0).getId();
            Long functionId = functionsForLinks.get(0).getId();

            Sort sortOrderDesc = Sort.by(Sort.Direction.DESC, "orderIndex");

            start = System.currentTimeMillis();
            compositeFunctionLinkRepository.findByCompositeFunctionId(compositeFunctionId, sortOrderDesc);
            duration = System.currentTimeMillis() - start;
            csv.append("CompositeFunctionLinkRepository,findByCompositeFunctionIdSortedByOrderDesc,").append(duration).append("\n");

            start = System.currentTimeMillis();
            compositeFunctionLinkRepository.findByFunctionId(functionId, sortOrderDesc);
            duration = System.currentTimeMillis() - start;
            csv.append("CompositeFunctionLinkRepository,findByFunctionIdSortedByOrderDesc,").append(duration).append("\n");
        }

        try (FileWriter writer = new FileWriter("sorting_performance_results.csv")) {
            writer.write(csv.toString());
        }

        System.out.println("Результаты готовы.");
    }
}