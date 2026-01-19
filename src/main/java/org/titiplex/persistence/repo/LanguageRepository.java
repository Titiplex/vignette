package org.titiplex.persistence.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Language;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, String> {
    @EntityGraph(attributePaths = {"family", "parent"})
    Page<Language> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"family", "parent"})
    Optional<Language> findWithFamilyAndParentById(String id);
}