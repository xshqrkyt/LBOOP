package com.lab5.framework.repository;

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
import java.util.Optional;

import org.springframework.data.domain.Sort;

@SpringBootTest
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class CompositeFunctionRepositoryTest {

    @Autowired
    private CompositeFunctionRepository compositeFunctionRepository;

    @Autowired
    private FunctionRepository functionRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setUsername("ownerCompFunc");
        owner.setPasswordHash("pass");
        owner.setEmail("ownercompfunc@example.com");
        owner.setRole(UserRole.USER);
        owner = userRepository.save(owner);
    }

    @Test
    public void testSaveAndFindByOwnerId() {
        CompositeFunction cf1 = createCompositeFunction("Composite 1");
        CompositeFunction cf2 = createCompositeFunction("Composite 2");

        compositeFunctionRepository.saveAll(List.of(cf1, cf2));
        compositeFunctionRepository.flush();

        List<CompositeFunction> list = compositeFunctionRepository.findByOwnerId(owner.getId());
        assertEquals(2, list.size());
    }

    @Test
    public void testFindByNameContainingIgnoreCase() {
        compositeFunctionRepository.save(createCompositeFunction("Alpha Composite"));
        compositeFunctionRepository.save(createCompositeFunction("Beta Composite"));
        compositeFunctionRepository.save(createCompositeFunction("Gamma"));

        List<CompositeFunction> results = compositeFunctionRepository.findByNameContainingIgnoreCase("composite");
        assertEquals(2, results.size());
    }

    @Test
    public void testFindByOwnerIdAndNameContainingIgnoreCase() {
        compositeFunctionRepository.save(createCompositeFunction("Test Composite A"));
        compositeFunctionRepository.save(createCompositeFunction("Test Composite B"));
        compositeFunctionRepository.save(createCompositeFunction("Other"));

        List<CompositeFunction> results = compositeFunctionRepository.findByOwnerIdAndNameContainingIgnoreCase(owner.getId(), "test composite");
        assertEquals(2, results.size());
    }

    @Test
    public void testFindByIdWithLinks() {
        CompositeFunction cf = createCompositeFunction("Function With Links");

        // Создаем функцию, на которую будут ссылаться CompositeFunctionLink
        Function linkedFunction = new Function();
        linkedFunction.setName("Linked Function");
        linkedFunction.setType("TABULATED");
        linkedFunction.setOwner(cf.getOwner());
        linkedFunction = functionRepository.save(linkedFunction);

        // Создаем ссылки и устанавливаем все обязательные связи
        CompositeFunctionLink link1 = new CompositeFunctionLink();
        link1.setCompositeFunction(cf);
        link1.setFunction(linkedFunction);
        link1.setOrderIndex(1);
        cf.getCompositeLinks().add(link1);

        CompositeFunctionLink link2 = new CompositeFunctionLink();
        link2.setCompositeFunction(cf);
        link2.setFunction(linkedFunction);
        link2.setOrderIndex(2);
        cf.getCompositeLinks().add(link2);

        // Сохраняем CompositeFunction с каскадной связью для CompositeFunctionLink
        CompositeFunction saved = compositeFunctionRepository.save(cf);

        Optional<CompositeFunction> found = compositeFunctionRepository.findByIdWithLinks(saved.getId());
        assertTrue(found.isPresent());
        assertEquals(2, found.get().getCompositeLinks().size());
        assertEquals("Function With Links", found.get().getName());
    }

    @Test
    public void testFindByOwnerIdWithLinksSorted() {
        CompositeFunction cf1 = createCompositeFunction("CompFunc A");
        CompositeFunction cf2 = createCompositeFunction("CompFunc B");

        compositeFunctionRepository.saveAll(List.of(cf2, cf1));
        compositeFunctionRepository.flush();

        List<CompositeFunction> list = compositeFunctionRepository.findByOwnerIdWithLinks(owner.getId(), Sort.by("name").ascending());
        assertEquals(2, list.size());
        assertEquals("CompFunc A", list.get(0).getName());
        assertEquals("CompFunc B", list.get(1).getName());
    }

    private CompositeFunction createCompositeFunction(String name) {
        CompositeFunction cf = new CompositeFunction();
        cf.setName(name);
        cf.setOwner(owner);
        return cf;
    }
}
