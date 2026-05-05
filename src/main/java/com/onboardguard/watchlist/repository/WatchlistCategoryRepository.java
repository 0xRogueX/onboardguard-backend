package com.onboardguard.watchlist.repository;

import com.onboardguard.shared.common.enums.CategoryCode;
import com.onboardguard.watchlist.entity.WatchlistCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WatchlistCategoryRepository extends JpaRepository<WatchlistCategory , Long> {

    Optional<WatchlistCategory> findByCode(CategoryCode code);

    List<WatchlistCategory> findByIsActiveTrue();
}
