package com.innowise.userservice.repository.specification;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public final class SpecificationHelper {

    private SpecificationHelper() {}

    public static <T> Page<T> findPage(EntityManager em, Class<T> entityClass,
                                       Specification<T> spec, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) cq.where(predicate);

        if (pageable.getSort().isSorted()) {
            cq.orderBy(pageable.getSort().stream()
                    .map(order -> order.isAscending()
                            ? cb.asc(root.get(order.getProperty()))
                            : cb.desc(root.get(order.getProperty())))
                    .toList());
        }

        TypedQuery<T> typedQuery = em.createQuery(cq);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        List<T> content = typedQuery.getResultList();

        Long total = count(em, entityClass, spec);
        return new PageImpl<>(content, pageable, total);
    }

    private static <T> Long count(EntityManager em, Class<T> entityClass, Specification<T> spec) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<T> root = cq.from(entityClass);
        Predicate predicate = spec.toPredicate(root, cq, cb);
        cq.select(cb.count(root));
        if (predicate != null) cq.where(predicate);
        return em.createQuery(cq).getSingleResult();
    }
}