package com.planitsquare.holiday.repository;

import com.planitsquare.holiday.constant.HolidaySortType;
import com.planitsquare.holiday.entity.HolidayInfoEntity;
import com.planitsquare.holiday.model.HolidayInfoDto;
import com.planitsquare.holiday.model.SearchFilterDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.planitsquare.holiday.entity.QHolidayInfoEntity.holidayInfoEntity;

import static com.planitsquare.holiday.entity.QHolidayCountryEntity.holidayCountryEntity;

import static com.planitsquare.holiday.entity.QHolidayTypeEntity.holidayTypeEntity;
import static com.querydsl.core.types.Order.ASC;
import static com.querydsl.core.types.Order.DESC;


@Repository
public class HolidayInfoQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    public HolidayInfoQueryDslRepository(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public Page<HolidayInfoDto> findHolidayInfoByYear(int year, PageRequest pageable, SearchFilterDto filter) {
        filter.setYear(year);

        Long count = findHolidayInfoCount(filter);
        List<HolidayInfoDto> results = findHolidayInfo(pageable, filter);

        return new PageImpl<>(results, pageable, count);

    }

    public Page<HolidayInfoDto> findHolidayInfoByCountry(String country, PageRequest pageable, SearchFilterDto filter) {
        filter.setCountry(country);

        Long count = findHolidayInfoCount(filter);
        List<HolidayInfoDto> results = findHolidayInfo(pageable, filter);

        return new PageImpl<>(results, pageable, count);
    }

    private List<HolidayInfoDto> findHolidayInfo(PageRequest pageable, SearchFilterDto filter) {

        JPAQuery<?> query = findHolidayInfoQuery(filter);

        Sort s = pageable.getSort();
        s.get().forEach(order -> {
            HolidaySortType sort = HolidaySortType.valueOfString(order.getProperty().toUpperCase());
            if(sort != null) {
                PathBuilder<HolidayInfoEntity> pathBuilder = new PathBuilder<>(HolidayInfoEntity.class, sort.getSortColumn());
                query.orderBy(new OrderSpecifier(order.isAscending() ? ASC : DESC, pathBuilder));
            }
        });

        return query.select(holidayInfoEntity)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch().stream().map(HolidayInfoEntity::makeDto).toList();
    }

    private Long findHolidayInfoCount(SearchFilterDto filter) {
        JPAQuery<?> query = findHolidayInfoQuery(filter);
        return query.select(holidayInfoEntity.count()).fetchOne();
    }



    private JPAQuery<?> findHolidayInfoQuery(SearchFilterDto filter) {

        JPAQuery<?> query = queryFactory.from(holidayInfoEntity)
                .leftJoin(holidayTypeEntity)
                .on(holidayInfoEntity.holidayInfoSeq.eq(holidayTypeEntity.holidayInfoSeq))
                .leftJoin(holidayCountryEntity)
                .on(holidayInfoEntity.holidayInfoSeq.eq(holidayCountryEntity.holidayInfoSeq));

        Predicate where = holidaySearchPredicate(filter);

        if (where != null) {
            query.where(where);
        }

        return query;
    }

    private Predicate holidaySearchPredicate(SearchFilterDto filter) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filter.getYear() != null) {
            builder.and(holidayInfoEntity.holidayYear.eq(filter.getYear()));
        }

        if (filter.getCountry() != null) {
            builder.and(holidayInfoEntity.countryCode.eq(filter.getCountry()));
        }

        if(filter.getToDate() != null) {
            builder.and(holidayInfoEntity.holidayDate.lt(filter.getToDate()));
        }

        if(filter.getFromDate() != null) {
            builder.and(holidayInfoEntity.holidayDate.gt(filter.getFromDate()));
        }

        if(filter.getTypes() != null) {
            builder.and(holidayTypeEntity.type.in(filter.getTypes()));
        }

        if(filter.getTypes() != null) {
            builder.and(holidayCountryEntity.country.in(filter.getCounties()));
        }

        return builder.hasValue() ? builder : null;
    }
}
