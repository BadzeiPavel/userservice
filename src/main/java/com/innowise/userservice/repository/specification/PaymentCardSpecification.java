package com.innowise.userservice.repository.specification;

import com.innowise.userservice.model.entity.PaymentCard;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class PaymentCardSpecification {

  public static Specification<PaymentCard> withFilters(String holder) {
    return (root, query, cb) -> {
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(cb.isFalse(root.get("deleted")));

      if (holder != null && !holder.isBlank()) {
        predicates.add(cb.like(cb.lower(root.get("holder")), "%" + holder.toLowerCase() + "%"));
      }
      return cb.and(predicates.toArray(new Predicate[0]));
    };
  }
}