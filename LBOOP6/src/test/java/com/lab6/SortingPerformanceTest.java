package com.lab6;

import com.lab6.enums.FunctionType;
import com.lab6.enums.UserRole;
import com.lab6.entity.*;
import com.lab6.repository.*;

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

        Long ownerId = createdUsers.get(0).getId();
        String namePart = allFunctions.get(0).getName(); // используем реально существующее полное имя
        String type = allFunctions.get(0).getType();     // тип из сохраненной сущности
        Sort sortByNameAsc = Sort.by(Sort.Direction.ASC, "name");

        start = System.currentTimeMillis();
        List<Function> result = functionRepository.findByNameAndTypeAndOwnerId(namePart, type, ownerId, sortByNameAsc);
        duration = System.currentTimeMillis() - start;

        csv.append("FunctionRepository,findByNameAndTypeAndOwnerIdSortedByNameAsc,").append(duration).append("\n");

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

        // Подготовим список имён для поиска (лучше заранее создать объекты с такими именами)
        List<String> namesToFind = List.of("CompFunction_abc", "CompFunction_xyz", "CompFunction_test");

        // Измерение времени для findByNameIn с сортировкой
        start = System.currentTimeMillis();
        List<CompositeFunction> foundWithSort = compositeFunctionRepository.findByNameIn(namesToFind, Sort.by(Sort.Direction.ASC, "name"));
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,findByNameInWithSort,").append(duration).append("\n");

        if (!allCompositeFunctions.isEmpty()) {
            // Измерение времени findAllByOrderById с сортировкой по id ASC
            start = System.currentTimeMillis();
            List<CompositeFunctionLink> linksAsc = compositeFunctionLinkRepository.findAllByOrderById(Sort.by(Sort.Direction.ASC, "id"));

            duration = System.currentTimeMillis() - start;
            csv.append("CompositeFunctionLinkRepository,findAllByOrderByIdSortedByIdAsc,").append(duration).append("\n");
            System.out.println("findAllByOrderById (ASC) returned " + linksAsc.size() + " entries in " + duration + " ms");

            // Измерение времени findAllByOrderById с сортировкой по id DESC
            start = System.currentTimeMillis();
            List<CompositeFunctionLink> linksDesc = compositeFunctionLinkRepository.findAllByOrderById(Sort.by(Sort.Direction.DESC, "id"));

            duration = System.currentTimeMillis() - start;
            csv.append("CompositeFunctionLinkRepository,findAllByOrderByIdSortedByIdDesc,").append(duration).append("\n");
            System.out.println("findAllByOrderById (DESC) returned " + linksDesc.size() + " entries in " + duration + " ms");
        }

        try (FileWriter writer = new FileWriter("sorting_performance_results.csv")) {
            writer.write(csv.toString());
        }

        System.out.println("Результаты готовы.");
    }
}