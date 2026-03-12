package org.titiplex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.persistence.model.Language;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LanguageImportServiceTest {

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private LanguageImportService languageImportService;

    @Test
    void loadLanguagesFromCsv_throwsOnMissingFile(@TempDir Path tempDir) {
        Path missing = tempDir.resolve("missing.csv");

        IOException ex = assertThrows(IOException.class, () -> languageImportService.loadLanguagesFromCsv(missing));

        assertTrue(ex.getMessage().contains("File does not exist"));
    }

    @Test
    void loadLanguagesFromCsv_normalizesBlankFields(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("languages.csv");
        Files.writeString(csv,
                """
                        id,name,bookkeeping,level,iso639_p3code,family_id,parent_id,description,markup_description,country_ids,child_family_count,child_language_count,child_dialect_count
                        fra,French,true,language,   ,  ,  ,  ,  ,   ,0,0,0
                        """);

        List<Language> result = languageImportService.loadLanguagesFromCsv(csv);

        assertEquals(1, result.size());
        Language language = result.get(0);
        assertNull(language.getIso639P3code());
        assertNull(language.getFamilyId());
        assertNull(language.getParentId());
        assertNull(language.getDescription());
        assertNull(language.getMarkupDescription());
        assertNull(language.getCountryIds());
    }

    @Test
    void importIfEmpty_returnsZeroWhenDataAlreadyExists(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("languages.csv");
        Files.writeString(csv,
                """
                        id,name,bookkeeping,level,child_family_count,child_language_count,child_dialect_count
                        fra,French,true,language,0,0,0
                        """);

        when(languageService.count()).thenReturn(12L);

        int inserted = languageImportService.importIfEmpty(csv);

        assertEquals(0, inserted);
        verify(languageService, never()).saveAll(anyList());
    }

    @Test
    void importIfEmpty_importsLanguagesWhenRepositoryIsEmpty(@TempDir Path tempDir) throws Exception {
        Path csv = tempDir.resolve("languages.csv");
        Files.writeString(csv,
                """
                        id,name,bookkeeping,level,child_family_count,child_language_count,child_dialect_count
                        fra,French,true,language,0,0,0
                        deu,German,true,language,0,0,0
                        """);

        when(languageService.count()).thenReturn(0L);
        when(languageService.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        int inserted = languageImportService.importIfEmpty(csv);

        assertEquals(2, inserted);
        verify(languageService, atLeastOnce()).saveAll(anyList());
    }
}