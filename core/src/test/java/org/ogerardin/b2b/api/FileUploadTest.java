package org.ogerardin.b2b.api;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.LocalTarget;
import org.ogerardin.b2b.domain.entity.PeerSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.storage.FileInfo;
import org.ogerardin.b2b.storage.RevisionNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.InputStream;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
//@Ignore
public class FileUploadTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private BackupTargetRepository targetRepository;

    @Autowired
    private BackupSetRepository backupSetRepository;

    @MockBean
    private StorageService storageService;

    @MockBean
    @Qualifier("gridFsStorageServiceFactory")
    private StorageServiceFactory storageServiceFactory;



    @Test
    @Ignore //FIXME need to mock BackupSetRepository for this to work
    public void shouldListAllFiles() throws Exception {
        String backupSetId = UUID.randomUUID().toString();

        given(this.storageServiceFactory.getStorageService(any(String.class)))
                .willReturn(storageService);

        given(this.storageService.getAllFiles(true))
                .willReturn(Stream.of(
                        new FileInfo(Paths.get("first.txt"), false),
                        new FileInfo(Paths.get("second.txt"), false)));

        this.mvc.perform(get(String.format("/api/backupsets/%s/files", backupSetId)))
                .andExpect(status().isOk())
                .andExpect(model().attribute("files",
                        Matchers.contains("first.txt", "second.txt")));
    }

    @Test
    public void shouldSaveUploadedFile() throws Exception {

        targetRepository.save(new LocalTarget());

        given(this.storageServiceFactory.getStorageService(any(String.class)))
                .willReturn(storageService);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt",
                "text/plain", "Spring Framework".getBytes());

        this.mvc.perform(MockMvcRequestBuilders.multipart("/api/peer/upload")
                    .file(multipartFile)
                    .param("computer-id", UUID.randomUUID().toString())
                )
                .andExpect(status().isOk());

        then(this.storageService).should().store(any(InputStream.class), eq("test.txt"));
    }

    @Test
    public void should400IfFilenameBlank() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("file", "",
                "text/plain", "Spring Framework".getBytes());
        this.mvc.perform(MockMvcRequestBuilders.multipart("/api/peer/upload")
                    .file(multipartFile)
                    .param("computer-id", UUID.randomUUID().toString())
                ).andExpect(status().isBadRequest());
    }

    @SuppressWarnings("unchecked")
    @Test
    @Ignore //FIXME
    public void should404WhenMissingFile() throws Exception {
        UUID computerId = UUID.randomUUID();
        BackupSet backupSet = new BackupSet();
        backupSet.setBackupTarget(new LocalTarget());
        PeerSource source = new PeerSource();
        source.setRemoteComputerId(computerId);
        backupSet.setBackupSource(source);
        backupSet = backupSetRepository.save(backupSet);

        given(this.storageServiceFactory.getStorageService(any(String.class)))
                .willReturn(storageService);

        given(this.storageService.getRevisionInfo(any(String.class))).willThrow(new RevisionNotFoundException("mock"));

        this.mvc.perform(get(String.format("/api/backupsets/%s/revisions/1", backupSet.getId())))
                .andExpect(status().isNotFound());
    }

}