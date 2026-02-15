package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.ImageUploadForm;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.ThumbnailRepository;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ThumbnailService {
    private final ThumbnailRepository repo;
    private final PasswordEncoder encoder;
    private final ScenarioService scenarioService;
    private final UserService userService;

    public ThumbnailService(ThumbnailRepository thumbnailRepository, PasswordEncoder encoder, ScenarioService scenarioService, UserService userService) {
        this.repo = thumbnailRepository;
        this.encoder = encoder;
        this.scenarioService = scenarioService;
        this.userService = userService;
    }

    @Transactional
    public Thumbnail save(String title, MultipartFile image, Scenario scenario, User user) throws IOException {
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setTitle(title);
        thumbnail.setImageBytes(image.getBytes());
        thumbnail.setImageSha256(encoder.encode(image.toString()));
        thumbnail.setScenarioId(scenario.getId());
        thumbnail.setScenario(scenario);
        thumbnail.setAuthorId(user.getId());
        thumbnail.setAuthor(user);
        return repo.save(thumbnail);
    }

    public List<Thumbnail> listByScenarioId(Long scenarioId) {
        return repo.getThumbnailsByScenarioId(scenarioId);
    }

    public Thumbnail getThumbnailById(Long id) {
        return repo.findById(id).orElseThrow();
    }
}
