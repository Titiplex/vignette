package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Author;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
