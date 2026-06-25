package com.myfschool.controller;

import com.myfschool.dto.response.ApiResponse;
import com.myfschool.service.CrudService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class  AbstractCrudController<T> {

    private final CrudService<T> service;

    protected AbstractCrudController(CrudService<T> service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<T>> findAll() {
        return ApiResponse.success(service.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<T> findById(@PathVariable Long id) {
        return ApiResponse.success(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<T>> create(@Valid @RequestBody T body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo mới thành công", service.create(body)));
    }

    @PutMapping("/{id}")
    public ApiResponse<T> update(@PathVariable Long id, @Valid @RequestBody T body) {
        return ApiResponse.success("Cập nhật thành công", service.update(id, body));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
