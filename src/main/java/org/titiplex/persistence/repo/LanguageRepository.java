package org.titiplex.persistence.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.titiplex.api.dto.LanguageOptionDto;
import org.titiplex.persistence.model.Language;

import java.util.Optional;

public interface LanguageRepository extends JpaRepository<Language, String> {
    @EntityGraph(attributePaths = {"family", "parent"})
    Page<Language> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"family", "parent"})
    Optional<Language> findWithFamilyAndParentById(String id);

    @Query("select new org.titiplex.api.dto.LanguageOptionDto(l.id, l.name) " +
            "from Language l " +
            "where (:q is null or lower(l.name) like lower(concat('%', :q, '%'))) " +
            "order by l.name")
    Page<LanguageOptionDto> listOptions(@Param("q") String q, Pageable pageable);
}
