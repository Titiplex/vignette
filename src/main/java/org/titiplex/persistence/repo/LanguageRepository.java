package org.titiplex.persistence.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.titiplex.api.dto.LanguageOption;
import org.titiplex.persistence.model.Language;

import java.util.List;
import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, String> {
    @EntityGraph(attributePaths = {"family", "parent"})
    Page<Language> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"family", "parent"})
    Optional<Language> findWithFamilyAndParentById(String id);

    @Query("select new org.titiplex.api.dto.LanguageOption(l.id, l.name) from Language l order by l.name")
    List<LanguageOption> listOptions();
}