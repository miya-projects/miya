package com.miya.common.model.dto.base;

import com.querydsl.core.QueryResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author 杨超辉
 * page 对象的简化版本， page对象序列化后有很多用不到的字段
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor(staticName = "of")
@Schema
public class Grid<T> implements EnvironmentAware {

    /**
     * 页码是否从1开始，默认为false，表示从0开始
     */
    public static boolean oneIndexedParameters = false;

    /**
     * 总页数
     */
    @NonNull
    @Schema(description = "总页数")
    private Integer totalPage;
    /**
     * 页大小
     */
    @NonNull
    @Schema(description = "页大小")
    private Integer pageSize;
    /**
     * 当前页数
     */
    @NonNull
    @Schema(description = "当前页数")
    private Integer currentPage;
    /**
     * 总条数
     */
    @NonNull
    @Schema(description = "总条数")
    private Long total;
    /**
     * 表格数据
     */
    @NonNull
    @Schema(description = "表格数据")
    private List<T> rows;

    @Schema(description = "额外的其他数据")
    private Map<String, Object> others;

    /**
     * 通过page对象转换为Grid对象
     *
     * @param page
     */
    public static <T> Grid<T> of(Page<T> page) {
        return of(page, null);
    }

    /**
     * 通过page对象转换为Grid对象
     * @param page
     * @param others 其他信息
     */
    public static <T> Grid<T> of(Page<T> page, Map<String, Object> others) {
        Pageable pageable = page.getPageable();
        final Grid<T> grid = Grid.of(page.getTotalPages(), pageable.getPageSize(),
                oneIndexedParameters?pageable.getPageNumber()+1:pageable.getPageNumber(), page.getTotalElements(), page.getContent());
        grid.setOthers(others);
        return grid;
    }

    public static <T> Grid<T> of(QueryResults<T> queryResults) {
        return Grid.of((int) Math.ceil(queryResults.getTotal() / (double)queryResults.getLimit()) + 1,
                (int) queryResults.getLimit() + 1,
                (int) Math.ceil(queryResults.getOffset() / (double)queryResults.getLimit()) + (oneIndexedParameters?1:0),
                queryResults.getTotal(),
                queryResults.getResults());
    }

    @Override
    public void setEnvironment(Environment environment) {
        // {org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties}
        oneIndexedParameters = environment.getProperty("spring.data.web.pageable.one-indexed-parameters", Boolean.class, false);
    }
}
