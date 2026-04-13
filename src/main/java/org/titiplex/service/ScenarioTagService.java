package org.titiplex.service;

import org.springframework.stereotype.Service;
import org.titiplex.api.dto.ScenarioTagSuggestionDto;
import org.titiplex.persistence.model.ScenarioTag;
import org.titiplex.persistence.repo.ScenarioTagRepository;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class ScenarioTagService {

    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private final ScenarioTagRepository repository;

    public ScenarioTagService(ScenarioTagRepository repository) {
        this.repository = repository;
    }

    public List<ScenarioTag> resolveTags(Collection<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }

        LinkedHashMap<String, String> unique = new LinkedHashMap<>();
        for (String raw : rawTags) {
            if (raw == null) continue;
            String cleaned = raw.trim().replaceAll("\\s+", " ");
            if (cleaned.isBlank()) continue;

            if (cleaned.length() > 64) {
                throw new IllegalArgumentException("Each tag must be at most 64 characters long");
            }

            String normalized = normalize(cleaned);
            if (normalized.isBlank()) {
                continue;
            }

            unique.putIfAbsent(normalized, cleaned);
        }

        List<ScenarioTag> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : unique.entrySet()) {
            String normalized = entry.getKey();
            String display = entry.getValue();

            ScenarioTag tag = repository.findByNormalizedName(normalized)
                    .orElseGet(() -> createTag(display, normalized));

            result.add(tag);
        }

        return result;
    }

    public List<ScenarioTagSuggestionDto> suggest(String query, int limit) {
        String q = query == null ? "" : query.trim();
        if (q.isBlank()) {
            return repository.findAll().stream()
                    .sorted(Comparator.comparing(ScenarioTag::getName, String.CASE_INSENSITIVE_ORDER))
                    .limit(limit)
                    .map(this::toDto)
                    .toList();
        }

        return repository.searchSuggestions(q).stream()
                .limit(limit)
                .map(this::toDto)
                .toList();
    }

    public List<String> toNames(Collection<ScenarioTag> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .map(ScenarioTag::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private ScenarioTagSuggestionDto toDto(ScenarioTag tag) {
        return new ScenarioTagSuggestionDto(tag.getId(), tag.getName(), tag.getNormalizedName());
    }

    private ScenarioTag createTag(String display, String normalized) {
        ScenarioTag tag = new ScenarioTag();
        tag.setName(display);
        tag.setNormalizedName(normalized);
        tag.setCreatedAt(Instant.now());
        return repository.save(tag);
    }

    public String normalize(String value) {
        String ascii = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();

        String normalized = NON_ALNUM.matcher(ascii).replaceAll("-");
        normalized = normalized.replaceAll("^-+|-+$", "");
        return normalized;
    }
}