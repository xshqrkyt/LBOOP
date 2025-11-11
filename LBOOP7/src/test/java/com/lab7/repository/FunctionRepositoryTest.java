package com.lab7.repository;

import com.lab7.enums.FunctionType;
import com.lab7.enums.UserRole;
import com.lab7.entity.*;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class FunctionRepositoryTest {

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Random random = new Random();

    @BeforeEach
    void setUp() {
        testUser = new User();
        // Уникальное имя пользователя для предотвращения конфликта
        testUser.setUsername("framework_user_" + UUID.randomUUID().toString().substring(0, 8));
        testUser.setPasswordHash("password123");
        // Email оставляем фиксированным
        testUser.setEmail("framework@example.com");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);
    }

    // Метод создания массива с рандомными значениями
    private double[] randomDoubleArray(int size) {
        double[] arr = new double[size];
        for (int i = 0; i < size; ++i)
            arr[i] = random.nextDouble() * 100; // значения от 0 до 100

        return arr;
    }

    // Создает Function со случайными значениями функции в Point
    private Function createFunctionWithRandomPoint() {
        Function function = new Function();
        function.setName("Function_" + UUID.randomUUID().toString().substring(0, 6));
        function.setType("SQR");
        function.setOwner(testUser);

        Point point = new Point();
        int size = 3 + random.nextInt(8); // длина массива 3–10
        point.setXValue(randomDoubleArray(size));
        point.setYValue(randomDoubleArray(size));
        point.setFunction(function);

        function.setPoints(point);
        return function;
    }

    @Test
    void testSaveFunctionWithRandomPoint() {
        Function function = createFunctionWithRandomPoint();

        Function saved = functionRepository.save(function);

        assertNotNull(saved.getId());
        assertNotNull(saved.getPoints());
        assertTrue(saved.getPoints().getXValue().length >= 3);
        assertEquals(saved.getPoints().getXValue().length, saved.getPoints().getYValue().length);
        assertEquals(testUser.getId(), saved.getOwner().getId());
        assertNotNull(saved.getName());
        assertNotNull(saved.getType());
    }

    @Test
    void testFindByOwnerIdWithPoint() {
        Function function1 = createFunction("Func1", "TABULATED");
        addPointsToFunction(function1, 1);
        functionRepository.save(function1);

        Function function2 = createFunction("Func2", "SQR");
        addPointsToFunction(function2, 2);
        functionRepository.save(function2);

        List<Function> functions = functionRepository.findByOwnerId(Long.valueOf(testUser.getId()));

        assertEquals(2, functions.size());
        assertTrue(functions.stream().allMatch(f -> f.getOwner().getId().equals(testUser.getId())));
        assertTrue(functions.stream().allMatch(f -> f.getPoints() != null));
    }

    @Test
    void testFindByOwnerId() {
        Function function1 = createFunction("Function 1", "TABULATED");
        Function function2 = createFunction("Function 2", "SQR");
        Function function3 = createFunction("Function 3", "IDENTITY");

        functionRepository.saveAll(List.of(function1, function2, function3));

        List<Function> functions = functionRepository.findByOwnerId(Long.valueOf(testUser.getId()));
        assertEquals(3, functions.size());
        assertTrue(functions.stream().allMatch(f -> f.getOwner().getId().equals(testUser.getId())));
    }

    @Test
    void testFindByName() {
        Function f1 = createFunction("Linear Function", "TABULATED");
        Function f2 = createFunction("Quadratic Function", "SQR");
        Function f3 = createFunction("Other", "IDENTITY");
        functionRepository.saveAll(List.of(f1, f2, f3));
        functionRepository.flush();

        List<Function> functionResults = functionRepository.findByName("Quadratic Function");
        assertEquals(1, functionResults.size());

        List<Function> linearResults = functionRepository.findByName("Linear Function");
        assertEquals(1, linearResults.size());
        assertEquals("Linear Function", linearResults.get(0).getName());
    }

    @Test
    public void testFindByType() {
        // Создаем несколько функций с разными типами
        Function f1 = createFunction("FuncType1", "TABULATED");
        Function f2 = createFunction("FuncType2", "SQR");
        Function f3 = createFunction("FuncType3", "TABULATED");

        functionRepository.saveAll(List.of(f1, f2, f3));

        // Ищем по типу "TABULATED"
        List<Function> result = functionRepository.findByType("TABULATED");
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(f -> "TABULATED".equals(f.getType()));
    }

    @Test
    void testFindByNameAndTypeAndOwnerIdWithSort() {
        // Создаем пользователя
        User owner = new User();
        owner.setUsername("owner_" + UUID.randomUUID().toString().substring(0, 8));
        owner.setPasswordHash("pass");
        owner.setEmail("owner@example.com");
        owner.setRole(UserRole.USER);
        owner = userRepository.save(owner);

        // Создаем функции с разными типами и именами
        Function f1 = new Function();
        f1.setName("TestFunc");
        f1.setType(FunctionType.DEBOOR.toString());  // Передаем Enum
        f1.setOwner(owner);

        Function f2 = new Function();
        f2.setName("TestFunc");
        f2.setType(FunctionType.SQR.toString());
        f2.setOwner(owner);

        Function f3 = new Function();
        f3.setName("DifferentFunc");
        f3.setType(FunctionType.DEBOOR.toString());
        f3.setOwner(owner);

        functionRepository.saveAll(List.of(f1, f2, f3));

        // Выполнение поиска по имени, типу и ownerId с сортировкой по имени
        List<Function> result = functionRepository.findByNameAndTypeAndOwnerId("TestFunc", FunctionType.DEBOOR.toString(), Long.valueOf(owner.getId()), Sort.by(Sort.Direction.ASC, "name"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TestFunc");
        assertThat(result.get(0).getType()).isEqualTo(FunctionType.DEBOOR.toString());
        assertThat(result.get(0).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void testFindByIdWithPoints() {
        Function function = createFunction("Function with Points", "TABULATED");
        addPointsToFunction(function, 1); // добавляем 1 точку, т.к. один Point
        Function saved = functionRepository.save(function);

        Optional<Function> found = functionRepository.findByIdWithPoints(saved.getId());
        assertTrue(found.isPresent());
        assertNotNull(found.get().getPoints());
        assertEquals("Function with Points", found.get().getName());
    }

    @Test
    void testFindByOwnerIdWithPoints() {
        Function function1 = createFunction("Function 1", "TABULATED");
        addPointsToFunction(function1, 1);
        functionRepository.save(function1);

        Function function2 = createFunction("Function 2", "SQR");
        addPointsToFunction(function2, 1);
        functionRepository.save(function2);

        List<Function> functions = functionRepository.findByOwnerIdWithPoints(Long.valueOf(testUser.getId()));
        assertEquals(2, functions.size());
        assertTrue(functions.stream().allMatch(f -> f.getPoints() != null));
    }

    @Test
    void testFindByOwnerIdOrderByCreatedAtDesc() {
        Function function1 = createFunction("Old Function", "TABULATED");
        functionRepository.save(function1);

        try {
            Thread.sleep(10);
        }

        catch (InterruptedException error) {}

        Function function2 = createFunction("New Function", "SQR");
        functionRepository.save(function2);

        List<Function> functions = functionRepository.findByOwnerIdOrderByCreatedAtDesc(Long.valueOf(testUser.getId()));
        assertEquals(2, functions.size());
        assertEquals("New Function", functions.get(0).getName());
        assertEquals("Old Function", functions.get(1).getName());
    }

    @Test
    void testFindByOwnerIdOrderByNameAsc() {
        Function functionC = createFunction("Charlie", "TABULATED");
        Function functionA = createFunction("Alpha", "SQR");
        Function functionB = createFunction("Beta", "IDENTITY");

        functionRepository.saveAll(List.of(functionA, functionB, functionC));

        List<Function> functions = functionRepository.findByOwnerIdOrderByNameAsc(Long.valueOf(testUser.getId()));
        assertEquals(3, functions.size());
        assertEquals("Alpha", functions.get(0).getName());
        assertEquals("Beta", functions.get(1).getName());
        assertEquals("Charlie", functions.get(2).getName());
    }

    private Function createFunction(String name, String type) {
        Function function = new Function();
        function.setName(name);
        function.setType(type);
        function.setOwner(testUser);
        return function;
    }

    private void addPointsToFunction(Function function, int i) {
        Point point = new Point();
        point.setXValue(new double[]{(double) i});
        point.setYValue(new double[]{(double) i * i});
        point.setFunction(function);
        function.setPoints(point);
    }
}