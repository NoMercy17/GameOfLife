package com.gameoflife;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRecordRepository extends JpaRepository<GameRecord, Integer> {

    @Query(value = "SELECT * FROM games ORDER BY id DESC LIMIT :n", nativeQuery = true)
    List<GameRecord> findlastNGames(@Param("n") int n);
}
