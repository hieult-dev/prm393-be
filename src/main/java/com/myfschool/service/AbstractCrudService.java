package com.myfschool.service;

import com.myfschool.entity.Identifiable;
import com.myfschool.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public abstract class AbstractCrudService<T extends Identifiable> implements CrudService<T> {

    private final JpaRepository<T, Long> repository;
    private final String resourceName;

    protected AbstractCrudService(JpaRepository<T, Long> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public T findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(resourceName, id));
    }

    @Override
    @Transactional
    public T create(T entity) {
        entity.setId(null);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public T update(Long id, T entity) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(resourceName, id);
        }
        entity.setId(id);
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(resourceName, id);
        }
        repository.deleteById(id);
    }
}
