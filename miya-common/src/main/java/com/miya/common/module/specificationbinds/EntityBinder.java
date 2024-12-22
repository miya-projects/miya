package com.miya.common.module.specificationbinds;

import com.miya.common.module.base.BaseEntity;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.CastUtils;

import java.util.*;

@RequiredArgsConstructor
public class EntityBinder {

    private final Set<String> includeFields = new HashSet<>();
    private final Set<String> excludeFields = new HashSet<>();

    private final Map<String, SpecificationBinder<? extends Path<?>>> fieldBindings;


    public void addIncludeField(String field) {
        includeFields.add(field);
    }

    public void addExcludeField(String field) {
        excludeFields.add(field);
    }

    public void addExcludeField(SingularAttribute<? extends BaseEntity, ?>... fields) {
        for (SingularAttribute<? extends BaseEntity, ?> field : fields) {
            excludeFields.add(field.getName());
        }
    }



    public boolean shouldBind(String field) {
        if (!includeFields.isEmpty()) {
            return includeFields.contains(field);
        }
        return !excludeFields.contains(field);
    }

    public <T> Predicate bind(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder, Map<String, String[]> parameterMap) {
        List<Predicate> predicates = new ArrayList<>();
        // 遍历 parameterMap，基于传入的 root 构建查询条件
        for (Map.Entry<String, String[]> paramEntry : parameterMap.entrySet()) {
            String fieldName = paramEntry.getKey();
            String[] values = paramEntry.getValue();

            Path<?> path = null;
            try {
                path = root.get(fieldName);
            } catch (Exception e) {
                //不属于po里的字段不处理 PathElementException
                continue;
            }
            if (!shouldBind(fieldName)) {
                continue;
            }
            //entityBinder
            SpecificationBinder<? extends Path<?>> specificationBinder = fieldBindings.get(fieldName);
            Predicate predicate;
            if (specificationBinder != null) {
                // 使用 BindType 生成查询条件
                // todo 这里泛型错误
                predicate = specificationBinder.buildPredicate(criteriaBuilder, CastUtils.cast(path), values);
            } else {
                // 没有绑定器的字段使用默认绑定器 SqmBasicValuedSimplePath
                predicate = DefaultSpecificationBinder.INSTANCE.buildPredicate(criteriaBuilder, path, values);
            }
            if (predicate != null) {
                predicates.add(predicate);
            }

            //// 检查 fieldBindings 中是否有对应字段的绑定方式
            //if (fieldBindings.containsKey(fieldName) && values != null && values.length > 0) {
            //    SpecificationBinder<?> specificationBinder = fieldBindings.get(fieldName);
            //}
        }

        // 如果没有匹配的参数，则返回空条件，否则返回组合的 AND 条件
        return predicates.isEmpty() ? criteriaBuilder.conjunction() : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
