package com.onboardguard.watchlist.repository;

import com.onboardguard.watchlist.entity.WatchlistCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistCategoryRepository extends JpaRepository<WatchlistCategory , Long> {
}
