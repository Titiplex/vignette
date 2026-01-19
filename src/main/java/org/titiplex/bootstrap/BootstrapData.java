package org.titiplex.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.titiplex.service.LanguageImportService;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BootstrapData implements ApplicationRunner {

    private final LanguageImportService importService;

    public BootstrapData(LanguageImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path csv = Paths.get("src/main/resources/glottolog/languoid.csv").toAbsolutePath();
        int inserted = importService.importIfEmpty(csv);
        if (inserted > 0) {
            System.out.println("Imported " + inserted + " languages.");
        } else {
            System.out.println("Languages already present, skipping import.");
        }
    }
}