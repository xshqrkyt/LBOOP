package com.lab6.repository;

import com.lab6.enums.FunctionType;
import com.lab6.enums.UserRole;
import com.lab6.entity.*;

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

    @BeforeEach
    void setUp() {
        testUser = new User();
        // Уникальное имя пользователя для предотвращения конфликта
        testUser.setUsername("framework_user_" + UUID.randomUUID().toString().substring(0, 8));
        testUser.setPasswordHash("password123");
        // Email оставляем фиксированным, как попросили
        testUser.setEmail("framework@example.com");
        testUser.setRole(UserRole.USER);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testSaveFunctionWithPoints() {
        Function function = new Function();
        function.setName("Test Function");
        function.setType("TABULATED");
        function.setOwner(testUser);

        Point point1 = new Point();
        point1.setXValue(1.0);
        point1.setYValue(1.0);
        point1.setFunction(function);
        function.getPoints().add(point1);

        Point point2 = new Point();
        point2.setXValue(2.0);
        point2.setYValue(4.0);
        point2.setFunction(function);
        function.getPoints().add(point2);

        Function saved = functionRepository.save(function);

        assertNotNull(saved.getId());
        assertEquals(2, saved.getPoints().size());
        assertEquals("Test Function", saved.getName());
        assertEquals(testUser.getId(), saved.getOwner().getId());
    }


    @Test
    void testFindByOwnerId() {
        // Create multiple functions
        Function function1 = createFunction("Function 1", "TABULATED");
        Function function2 = createFunction("Function 2", "SQR");
        Function function3 = createFunction("Function 3", "IDENTITY");

        functionRepository.saveAll(List.of(function1, function2, function3));

        List<Function> functions = functionRepository.findByOwnerId(testUser.getId());
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
        List<Function> result = functionRepository.findByNameAndTypeAndOwnerId("TestFunc", FunctionType.DEBOOR.toString(), owner.getId(), Sort.by(Sort.Direction.ASC, "name"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("TestFunc");
        assertThat(result.get(0).getType()).isEqualTo(FunctionType.DEBOOR.toString());
        assertThat(result.get(0).getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void testFindByIdWithPoints() {
        Function function = createFunction("Function with Points", "TABULATED");
        addPointsToFunction(function, 3);
        Function saved = functionRepository.save(function);

        Optional<Function> found = functionRepository.findByIdWithPoints(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getPoints().size());
        assertEquals("Function with Points", found.get().getName());
    }

    @Test
    void testFindByOwnerIdWithPoints() {
        Function function1 = createFunction("Function 1", "TABULATED");
        addPointsToFunction(function1, 2);
        functionRepository.save(function1);

        Function function2 = createFunction("Function 2", "SQR");
        addPointsToFunction(function2, 3);
        functionRepository.save(function2);

        List<Function> functions = functionRepository.findByOwnerIdWithPoints(testUser.getId());
        assertEquals(2, functions.size());
        assertTrue(functions.stream().allMatch(f -> f.getPoints().size() > 0));
    }

    @Test
    void testFindByOwnerIdOrderByCreatedAtDesc() {
        Function function1 = createFunction("Old Function", "TABULATED");
        functionRepository.save(function1);

        // Add small delay to ensure different timestamps
        try {
            Thread.sleep(10);
        }

        catch (InterruptedException error) {}

        Function function2 = createFunction("New Function", "SQR");
        functionRepository.save(function2);

        List<Function> functions = functionRepository.findByOwnerIdOrderByCreatedAtDesc(testUser.getId());
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

        List<Function> functions = functionRepository.findByOwnerIdOrderByNameAsc(testUser.getId());
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

    private void addPointsToFunction(Function function, int count) {
        for (int i = 1; i <= count; i++) {
            Point point = new Point();
            point.setXValue((double) i);
            point.setYValue((double) i * i);
            point.setFunction(function);
            function.getPoints().add(point);
        }
    }
}