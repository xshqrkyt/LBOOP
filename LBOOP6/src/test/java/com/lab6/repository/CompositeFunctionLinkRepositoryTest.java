package com.lab6.repository;

import com.lab6.entity.*;
import com.lab6.enums.UserRole;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.data.domain.Sort;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class CompositeFunctionLinkRepositoryTest {
    @Autowired
    private CompositeFunctionLinkRepository compositeFunctionLinkRepository;

    @Autowired
    private CompositeFunctionRepository compositeFunctionRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private CompositeFunction compositeFunction;
    private Function function;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setUsername("user1");
        owner.setPasswordHash("pass");
        owner.setEmail("user1@example.com");
        owner.setRole(UserRole.USER);
        owner = userRepository.save(owner);

        compositeFunction = new CompositeFunction();
        compositeFunction.setName("Composite Function 1");
        compositeFunction.setOwner(owner);
        compositeFunction = compositeFunctionRepository.save(compositeFunction);

        function = new Function();
        function.setName("Function 1");
        function.setType("TABULATED");
        function.setOwner(owner);
        function = functionRepository.save(function);
    }

    @Test
    public void testSaveAndFindByCompositeFunctionId() {
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(2);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(1);

        compositeFunctionLinkRepository.saveAll(List.of(link1, link2));

        List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByCompositeFunctionId(compositeFunction.getId());

        assertEquals(2, links.size());
        assertEquals(2, links.get(0).getOrderIndex());
        assertEquals(1, links.get(1).getOrderIndex());
    }

    @Test
    public void testFindByFunctionId() {
        CompositeFunctionLink link = new CompositeFunctionLink();
        link.setCompositeFunction(compositeFunction);
        link.setFunction(function);
        link.setOrderIndex(1);

        compositeFunctionLinkRepository.save(link);

        List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByFunctionId(function.getId());
        assertFalse(links.isEmpty());
        assertEquals(function.getId(), links.get(0).getFunction().getId());
    }

    @Test
    public void testFindByCompositeFunctionId() {
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(1);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(2);
        compositeFunctionLinkRepository.saveAll(List.of(link1, link2));

        List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByCompositeFunctionId(compositeFunction.getId());

        assertEquals(2, links.size());
        assertTrue(links.stream().anyMatch(l -> l.getOrderIndex() == 1));
        assertTrue(links.stream().anyMatch(l -> l.getOrderIndex() == 2));
    }

    @Test
    public void testFindByIdIn() {
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(1);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(2);

        compositeFunctionLinkRepository.saveAll(List.of(link1, link2));

        List<Long> ids = List.of(link1.getId(), link2.getId());
        List<CompositeFunctionLink> found = compositeFunctionLinkRepository.findByIdIn(ids);

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(l -> l.getId().equals(link1.getId())));
        assertTrue(found.stream().anyMatch(l -> l.getId().equals(link2.getId())));
    }

    @Test
    public void testFindAllSortedByOrderIndex() {
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(2);
        compositeFunctionLinkRepository.save(link1);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(1);
        compositeFunctionLinkRepository.save(link2);

        List<CompositeFunctionLink> list = compositeFunctionLinkRepository.findAll(Sort.by(Sort.Direction.ASC, "orderIndex"));

        assertThat(list).extracting(CompositeFunctionLink::getOrderIndex).isSorted();
    }

    @Test
    public void testFindAllByOrderByIdWithSort() {
        // Создаем и сохраняем объекты CompositeFunctionLink с обязательными полями
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(1); // обязательно, чтобы избежать ошибки not-null
        compositeFunctionLinkRepository.save(link1);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(2);
        compositeFunctionLinkRepository.save(link2);

        compositeFunctionLinkRepository.flush();

        // Получаем список с сортировкой по id возрастанию
        List<CompositeFunctionLink> asc = compositeFunctionLinkRepository.findAllByOrderById(Sort.by(Sort.Direction.ASC, "id"));
        assertThat(asc).isNotEmpty();
        assertThat(asc).extracting(CompositeFunctionLink::getId).isSorted();

        // Получаем список с сортировкой по id убыванию
        List<CompositeFunctionLink> desc = compositeFunctionLinkRepository.findAllByOrderById(Sort.by(Sort.Direction.DESC, "id"));
        assertThat(desc).isNotEmpty();
        assertThat(desc).extracting(CompositeFunctionLink::getId).isSortedAccordingTo((a, b) -> a.compareTo(b));
    }
}