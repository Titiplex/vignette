package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.ThumbnailRepository;
import org.titiplex.service.storage.FileStorageService;
import org.titiplex.service.storage.MediaContent;
import org.titiplex.service.storage.StoredFile;

import java.util.List;

@Service
public class ThumbnailService {

    private final ThumbnailRepository repo;
    private final FileStorageService storage;

    public ThumbnailService(ThumbnailRepository repo, FileStorageService storage) {
        this.repo = repo;
        this.storage = storage;
    }

    @Transactional
    public Thumbnail save(String title, MultipartFile image, Scenario scenario, User user) throws Exception {
        StoredFile stored = storage.storeThumbnail(image, scenario.getId());

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setTitle((title == null || title.isBlank()) ? "Image " + (repo.maxIdx(scenario.getId()) + 1) : title.trim());
        thumbnail.setScenarioId(scenario.getId());
        thumbnail.setScenario(scenario);
        thumbnail.setAuthorId(user.getId());
        thumbnail.setAuthor(user);
        thumbnail.setIdx(repo.maxIdx(scenario.getId()) + 1);
        thumbnail.setContentType(stored.contentType());
        thumbnail.setImageSha256(stored.sha256());
        thumbnail.setStoragePath(stored.relativePath());
        thumbnail.setSizeBytes(stored.sizeBytes());
        thumbnail.setOriginalFilename(stored.originalFilename());

        return repo.save(thumbnail);
    }

    public List<Thumbnail> listByScenarioId(Long scenarioId) {
        return repo.findByScenarioIdOrderByIdxAsc(scenarioId);
    }

    public Thumbnail getThumbnailById(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public MediaContent loadContent(Long id) {
        Thumbnail t = getThumbnailById(id);
        return storage.load(t.getStoragePath(), t.getContentType(), t.getImageSha256());
    }
}