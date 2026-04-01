package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.titiplex.api.dto.LanguageDto;
import org.titiplex.api.dto.LanguageOptionDto;
import org.titiplex.persistence.model.Language;
import org.titiplex.persistence.repo.LanguageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class LanguageService {

    private final LanguageRepository repo;

    public LanguageService(LanguageRepository repo) {
        this.repo = repo;
    }

    public Page<Language> listLanguages(String query, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        String normalizedQuery = (query == null || query.isBlank()) ? null : query.trim();
        return repo.search(normalizedQuery, pageable);
    }

    public Page<LanguageDto> listLanguagesDto(int page, int size) {
        return listLanguages(null, page, size).map(this::toDto);
    }

    public Language getLanguage(String id) {
        var l = new Language();
        l.setName("Language not found");
        return repo.findById(id).orElse(l);
    }

    public LanguageDto toDto(Language l) {
        String familyName = (l.getFamily() != null) ? l.getFamily().getName() : null;
        String parentName = (l.getParent() != null) ? l.getParent().getName() : null;

        return new LanguageDto(
                l.getId(),
                l.getName(),
                l.getLevel(),
                l.getBookkeeping(),
                l.getIso639P3code(),
                l.getLatitude(),
                l.getLongitude(),
                l.getCountryIds(),
                l.getFamilyId(),
                familyName,
                l.getParentId(),
                parentName
        );
    }

    public LanguageDto getOneDto(String id) {
        Language l = repo.findWithFamilyAndParentById(id).orElseThrow();
        return toDto(l);
    }

    public long count() {
        return repo.count();
    }

    @Transactional
    public List<Language> saveAll(List<Language> languages) {
        return new ArrayList<>(repo.saveAll(languages));
    }

    public boolean existsById(String id) {
        return repo.existsById(id);
    }

    public Page<LanguageOptionDto> searchOptions(String q, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return repo.listOptions(q, pageable);
    }

    public List<Language> getLanguagesByFamily(String familyId) {
        Optional<Language> lang = repo.findById(familyId);
        if (lang.isEmpty() || !Objects.equals(lang.get().getLevel().toLowerCase(), "family")) {
            return List.of();
        }
        var stack = repo.findAllByFamilyId(familyId);
        List<Language> result = new ArrayList<>();

        while (!stack.isEmpty()) {
            var temp = stack.getFirst();
            stack.removeFirst();
            if (temp.getLevel().equalsIgnoreCase("family") || temp.getLevel().equalsIgnoreCase("parent")) {
                stack.addAll(repo.findAllByFamilyId(temp.getId()));
            } else {
                result.add(temp);
            }
        }

        return result;
    }
}
