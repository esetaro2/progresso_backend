package com.progresso.backend.repository;

import com.progresso.backend.model.Team;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

  Optional<Team> findByName(String name);

  List<Team> findByProjectManagerId(Long id);

  List<Team> findByMembersId(Long id);

  Boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

  Boolean existsByNameIgnoreCase(String name);
}
