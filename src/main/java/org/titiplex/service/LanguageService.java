package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.titiplex.api.dto.LanguageDto;
import org.titiplex.api.dto.LanguageOptionDto;
import org.titiplex.persistence.model.Language;
import org.titiplex.persistence.repo.LanguageRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class LanguageService {

    private final LanguageRepository repo;

    public LanguageService(LanguageRepository repo) {
        this.repo = repo;
    }

    public Page<Language> listLanguages(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 200);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("name").ascending());
        return repo.findAll(pageable);
    }

    public Page<LanguageDto> listLanguagesDto(int page, int size) {
        return listLanguages(page, size).map(this::toDto);
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
}
