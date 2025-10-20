package ru.ssau.tk.samsa.lb6.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ssau.tk.samsa.lb6.repo.FunctionRepository;
import ru.ssau.tk.samsa.lb6.model.Function;

import java.util.List;
import java.util.Optional;

@Service
public class FunctionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionService.class);
    private final FunctionRepository functionRepository;

    public FunctionService(FunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    public Function create(Function f) {
        Function saved = functionRepository.save(f);
        LOGGER.info("Created function id={}", saved.getId());
        return saved;
    }

    public Optional<Function> findById(Long id) {
        return functionRepository.findById(id);
    }

    public List<Function> findByOwner(Long ownerId) {
        return functionRepository.findByOwnerId(ownerId);
    }
}
