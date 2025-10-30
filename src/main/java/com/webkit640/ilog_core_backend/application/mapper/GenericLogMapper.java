package com.webkit640.ilog_core_backend.application.mapper;

import java.util.List;
import java.util.stream.Collectors;

public interface GenericLogMapper<E, D, R> {
    //개별 엔티티를 DTO로 변환
    D toDto(E entity);

    default List<D> toDtoList(List<E> entities){
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    R toResponse(List<E> entities);
}
