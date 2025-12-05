package com.planitsquare.holiday.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

@Repository
public class HolidayInfoQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public HolidayInfoQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

}
