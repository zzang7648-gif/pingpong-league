package com.mg.pingpong.repository;

import com.mg.pingpong.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findAllByOrderByPlayerNameAsc();
    Optional<Equipment> findByPlayerName(String playerName);
}