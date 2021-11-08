package com.miya.common.service.mapper;

import java.util.List;

public interface DTOMapper<D, E> {

    /**
     * Entity转DTO
     * @param entity /
     * @return /
     */
    D toDto(E entity);

    /**
     * Entity集合转DTO集合
     * @param entityList /
     * @return /
     */
    List<D> toDto(List<E> entityList);
}
