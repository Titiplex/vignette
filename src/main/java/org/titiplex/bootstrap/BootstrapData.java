package org.titiplex.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.titiplex.persistence.model.Role;
import org.titiplex.service.LanguageImportService;
import org.titiplex.service.RolesService;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class BootstrapData implements ApplicationRunner {

    private final LanguageImportService importService;
    private final RolesService rolesService;

    public BootstrapData(LanguageImportService importService,  RolesService rolesService) {
        this.importService = importService;
        this.rolesService = rolesService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // load roles
        rolesService.loadRoles();

        // Load languages
        Path csv = Paths.get("src/main/resources/glottolog/languoid.csv").toAbsolutePath();
        int inserted = importService.importIfEmpty(csv);
        if (inserted > 0) {
            System.out.println("Imported " + inserted + " languages.");
        } else {
            System.out.println("Languages already present, skipping import.");
        }
    }
}