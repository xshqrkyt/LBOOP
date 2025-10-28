package com.lab5.repository;

import com.lab5.common.enums.*;
import com.lab5.entity.*;
import com.lab5.repository.*;

import org.junit.jupiter.api.*;
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

        List<CompositeFunctionLink> links = compositeFunctionLinkRepository.findByCompositeFunctionId(compositeFunction.getId(), Sort.by("orderIndex").ascending());

        assertEquals(2, links.size());
        assertEquals(1, links.get(0).getOrderIndex());
        assertEquals(2, links.get(1).getOrderIndex());
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
    public void testFindByFunctionIdWithSorting() {
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(compositeFunction);
        link1.setFunction(function);
        link1.setOrderIndex(2);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(compositeFunction);
        link2.setFunction(function);
        link2.setOrderIndex(1);

        compositeFunctionLinkRepository.saveAll(List.of(link1, link2));

        List<CompositeFunctionLink> sortedLinks = compositeFunctionLinkRepository.findByFunctionId(function.getId(), Sort.by("orderIndex").ascending());

        assertEquals(2, sortedLinks.size());
        assertEquals(1, sortedLinks.get(0).getOrderIndex());
        assertEquals(2, sortedLinks.get(1).getOrderIndex());
    }
}