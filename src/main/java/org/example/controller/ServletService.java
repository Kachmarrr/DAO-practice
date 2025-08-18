package org.example.controller;

import java.util.List;

public interface ServletService<T> {
    List<T> findAll();
    T findById(Long id);
    T create(T entity);
    void update(T entity);
    void delete(Long id);
}
