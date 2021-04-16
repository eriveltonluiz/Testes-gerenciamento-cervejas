package project.api.rest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import project.api.rest.entity.Beer;

import java.util.Optional;

public interface BeerRepository extends JpaRepository<Beer, Long> {

    Optional<Beer> findByName(String name);
}
