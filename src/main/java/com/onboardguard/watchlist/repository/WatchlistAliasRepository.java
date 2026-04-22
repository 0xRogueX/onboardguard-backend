package com.onboardguard.watchlist.repository;

import com.onboardguard.watchlist.entity.WatchlistAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchlistAliasRepository extends JpaRepository<WatchlistAlias  , Long> {

}
