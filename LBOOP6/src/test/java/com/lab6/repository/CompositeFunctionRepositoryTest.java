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
    public void testFindByNameInWithoutSort() {
        CompositeFunction cf1 = createCompositeFunction("FuncA");
        CompositeFunction cf2 = createCompositeFunction("FuncB");
        CompositeFunction cf3 = createCompositeFunction("FuncC");
        compositeFunctionRepository.saveAll(List.of(cf1, cf2, cf3));

        List<CompositeFunction> found = compositeFunctionRepository.findByNameIn(List.of("FuncA", "FuncC"));

        assertThat(found).hasSize(2);
        assertThat(found).extracting(CompositeFunction::getName).containsExactlyInAnyOrder("FuncA", "FuncC");
    }

    @Test
    public void testFindByNameInWithSort() {
        CompositeFunction cf1 = createCompositeFunction("AAA");
        CompositeFunction cf2 = createCompositeFunction("BBB");
        CompositeFunction cf3 = createCompositeFunction("CCC");
        compositeFunctionRepository.saveAll(List.of(cf1, cf2, cf3));

        List<CompositeFunction> found = compositeFunctionRepository.findByNameIn(
                List.of("AAA", "BBB", "CCC"),
                Sort.by(Sort.Direction.DESC, "name")
        );

        assertThat(found).hasSize(3);
        assertThat(found.get(0).getName()).isEqualTo("CCC");
        assertThat(found.get(1).getName()).isEqualTo("BBB");
        assertThat(found.get(2).getName()).isEqualTo("AAA");
    }

    @Test
    public void testFindByNameExact() {
        CompositeFunction cf = createCompositeFunction("ExactFunc");
        compositeFunctionRepository.save(cf);

        List<CompositeFunction> found = compositeFunctionRepository.findByName("ExactFunc");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("ExactFunc");
    }

    private CompositeFunction createCompositeFunction(String name) {
        CompositeFunction cf = new CompositeFunction();
        cf.setName(name);
        cf.setOwner(owner);
        return cf;
    }
}
