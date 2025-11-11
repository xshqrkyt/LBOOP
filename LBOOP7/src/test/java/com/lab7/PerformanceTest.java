package com.lab7;

import com.lab7.entity.*;
import com.lab7.enums.FunctionType;
import com.lab7.enums.UserRole;
import com.lab7.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.junit.jupiter.api.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class PerformanceTest {

    @Autowired private UserRepository userRepository;
    @Autowired private FunctionRepository functionRepository;
    @Autowired private PointRepository pointRepository;
    @Autowired private CompositeFunctionRepository compositeFunctionRepository;
    @Autowired private CompositeFunctionLinkRepository compositeFunctionLinkRepository;

    private final int RECORDS = 10000;
    private final Random random = new Random();

    private List<User> createdUsers;

    private String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private FunctionType randomFunctionType() {
        return FunctionType.values()[random.nextInt(FunctionType.values().length)];
    }

    private double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    @BeforeEach
    public void setup() {
        createdUsers = new ArrayList<>();
    }

    @Test
    public void PerformanceTests() throws IOException {
        StringBuilder csv = new StringBuilder("repository,query,duration (ms)\n");

        // Создание пользователей
        long start = System.currentTimeMillis();
        for (int i = 0; i < RECORDS; i++) {
            User user = new User();
            user.setUsername("user_" + randomString());
            user.setEmail("user" + i + "@example.com");
            user.setPasswordHash(randomString());
            user.setRole(UserRole.USER);
            user = userRepository.save(user);
            createdUsers.add(user);
            if (i % 1000 == 0) userRepository.flush();
        }

        userRepository.flush();
        long duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,create,").append(duration).append("\n");

        // Поиск пользователей по id
        start = System.currentTimeMillis();
        for (User user : createdUsers)
            userRepository.findById(Long.valueOf(user.getId()));

        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,findById,").append(duration).append("\n");

        start = System.currentTimeMillis();
        if (!createdUsers.isEmpty())
            userRepository.findByUsername(createdUsers.get(0).getUsername());

        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,findByUsername,").append(duration).append("\n");

        start = System.currentTimeMillis();
        userRepository.findByEmailAndRole("example.com", UserRole.USER);
        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,findByEmailAndRole,").append(duration).append("\n");

        // Обновление пользователей
        start = System.currentTimeMillis();
        for (User user : createdUsers) {
            user.setEmail("updated_" + user.getEmail());
            userRepository.save(user);
        }

        userRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,update,").append(duration).append("\n");

        // Функции
        List<Function> allFunctions = new ArrayList<>();
        start = System.currentTimeMillis();
        int count = 0;
        for (User user : createdUsers) {
            Function func = new Function();
            func.setName("Function_" + randomString());
            func.setType(randomFunctionType().name());
            func.setOwner(user);
            functionRepository.save(func);
            allFunctions.add(func);
            if (++count % 1000 == 0) functionRepository.flush();
        }

        functionRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,create,").append(duration).append("\n");

        // Поиск функций
        start = System.currentTimeMillis();
        for (Function func : allFunctions)
            functionRepository.findById(func.getId());

        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,findById,").append(duration).append("\n");

        start = System.currentTimeMillis();
        functionRepository.findByOwnerId(Long.valueOf(createdUsers.get(0).getId()));
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,findByOwnerId,").append(duration).append("\n");

        start = System.currentTimeMillis();
        functionRepository.findByName("Function_");
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,findByName,").append(duration).append("\n");

        start = System.currentTimeMillis();
        functionRepository.findByType(randomFunctionType().name());
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,findByType,").append(duration).append("\n");

        // Обновление функций
        start = System.currentTimeMillis();
        for (Function f : allFunctions) {
            f.setName("Updated_" + f.getName());
            functionRepository.save(f);
        }

        functionRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,update,").append(duration).append("\n");

        // Создание точек
        List<Point> allPoints = new ArrayList<>();
        start = System.currentTimeMillis();
        count = 0;
        for (Function func : allFunctions) {
            // Создаем по одной точке для каждой функции
            Point point = new Point();
            double[] xValues = new double[5];
            double[] yValues = new double[5];

            for (int i = 0; i < 5; i++) {
                xValues[i] = randomDouble(-1000, 1000);
                yValues[i] = randomDouble(-1000, 1000);
            }

            point.setXValue(xValues);
            point.setYValue(yValues);
            point.setFunction(func);
            pointRepository.save(point);
            allPoints.add(point);
        }

        pointRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,create,").append(duration).append("\n");

        // Поиск точек
        start = System.currentTimeMillis();
        for (Point p : allPoints)
            pointRepository.findById(p.getId());

        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,findById,").append(duration).append("\n");

        // Поиск по functionId
        start = System.currentTimeMillis();
        pointRepository.findByFunctionId(allFunctions.get(0).getId());
        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,findByFunctionId,").append(duration).append("\n");

        // Поиск по ownerId через функцию
        start = System.currentTimeMillis();
        pointRepository.findByFunctionOwnerId(Long.valueOf(createdUsers.get(0).getId()));
        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,findByFunctionOwnerId,").append(duration).append("\n");

        // Обновление точек
        start = System.currentTimeMillis();
        for (Point p : allPoints) {
            double[] newXValues = new double[5];
            double[] newYValues = new double[5];

            for (int i = 0; i < 5; ++i) {
                newXValues[i] = randomDouble(-1000, 1000);
                newYValues[i] = randomDouble(-1000, 1000);
            }

            p.setXValue(newXValues);
            p.setYValue(newYValues);
            pointRepository.save(p);
        }

        pointRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,update,").append(duration).append("\n");

        // CompositeFunction и ссылки
        List<CompositeFunction> allCompositeFunctions = new ArrayList<>();
        List<CompositeFunctionLink> allCompositeLinks = new ArrayList<>();
        List<Function> functionsForLinks = new ArrayList<>(allFunctions);
        start = System.currentTimeMillis();
        count = 0;

        for (User user : createdUsers) {
            CompositeFunction compositeFunction = new CompositeFunction();
            compositeFunction.setName("CompFunction_" + randomString());
            compositeFunction.setOwner(user);
            compositeFunction = compositeFunctionRepository.save(compositeFunction);
            allCompositeFunctions.add(compositeFunction);

            CompositeFunctionLink link = new CompositeFunctionLink();
            link.setCompositeFunction(compositeFunction);
            link.setFunction(functionsForLinks.get(random.nextInt(functionsForLinks.size())));
            link.setOrderIndex(count);
            compositeFunctionLinkRepository.save(link);
            allCompositeLinks.add(link);

            if (++count % 1000 == 0) {
                compositeFunctionRepository.flush();
                compositeFunctionLinkRepository.flush();
            }
        }

        compositeFunctionRepository.flush();
        compositeFunctionLinkRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,create,").append(duration).append("\n");
        csv.append("CompositeFunctionLinkRepository,create,").append(duration).append("\n");

        // Поиск CompositeFunction
        start = System.currentTimeMillis();
        for (CompositeFunction cf : allCompositeFunctions)
            compositeFunctionRepository.findById(cf.getId());

        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,findById,").append(duration).append("\n");

        start = System.currentTimeMillis();
        compositeFunctionRepository.findByOwnerId(Long.valueOf(createdUsers.get(0).getId()));
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,findByOwnerId,").append(duration).append("\n");

        // Подготовим список имён для поиска (лучше заранее создать объекты с такими именами)
        List<String> namesToFind = List.of("CompFunction_abc", "CompFunction_xyz", "CompFunction_test");

        // Измерение времени выполнения findByNameIn без сортировки
        start = System.currentTimeMillis();
        List<CompositeFunction> foundWithoutSort = compositeFunctionRepository.findByNameIn(namesToFind);
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,findByNameIn,").append(duration).append("\n");

        start = System.currentTimeMillis();
        compositeFunctionRepository.findByName("CompFunction");
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,findByName,").append(duration).append("\n");

        // Обновление CompositeFunction
        start = System.currentTimeMillis();
        for (CompositeFunction cf : allCompositeFunctions) {
            cf.setName("Updated_" + cf.getName());
            compositeFunctionRepository.save(cf);
        }

        compositeFunctionRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,update,").append(duration).append("\n");

        // Поиск CompositeFunctionLink
        start = System.currentTimeMillis();
        for (CompositeFunctionLink link : allCompositeLinks)
            compositeFunctionLinkRepository.findById(link.getId());

        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionLinkRepository,findById,").append(duration).append("\n");

        start = System.currentTimeMillis();
        compositeFunctionLinkRepository.findByCompositeFunctionId(allCompositeFunctions.get(0).getId());
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionLinkRepository,findByCompositeFunctionId,").append(duration).append("\n");

        start = System.currentTimeMillis();
        compositeFunctionLinkRepository.findByFunctionId(functionsForLinks.get(0).getId());
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionLinkRepository,findByFunctionId,").append(duration).append("\n");

        // Обновление CompositeFunctionLink
        start = System.currentTimeMillis();
        for (CompositeFunctionLink link : allCompositeLinks) {
            link.setOrderIndex(link.getOrderIndex() + 1);
            compositeFunctionLinkRepository.save(link);
        }

        compositeFunctionLinkRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionLinkRepository,update,").append(duration).append("\n");

        // Удаление CompositeFunctionLink
        start = System.currentTimeMillis();
        compositeFunctionLinkRepository.deleteAll();
        compositeFunctionLinkRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionLinkRepository,delete,").append(duration).append("\n");

        // Удаление CompositeFunction
        start = System.currentTimeMillis();
        compositeFunctionRepository.deleteAll();
        compositeFunctionRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("CompositeFunctionRepository,delete,").append(duration).append("\n");

        // Удаление Point
        start = System.currentTimeMillis();
        pointRepository.deleteAll();
        pointRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("PointRepository,delete,").append(duration).append("\n");

        // Удаление Function
        start = System.currentTimeMillis();
        functionRepository.deleteAll();
        functionRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("FunctionRepository,delete,").append(duration).append("\n");

        // Удаление User
        start = System.currentTimeMillis();
        userRepository.deleteAll();
        userRepository.flush();
        duration = System.currentTimeMillis() - start;
        csv.append("UserRepository,delete,").append(duration).append("\n");

        try (FileWriter writer = new FileWriter("performance_results.csv")) {
            writer.write(csv.toString());
        }

        System.out.println("Результаты готовы.");
    }
}