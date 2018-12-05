package org.ogerardin.b2b.api;

import com.google.common.collect.MoreCollectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.entity.BackupSet;
import org.ogerardin.b2b.domain.entity.PeerSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.storage.RevisionInfo;
import org.ogerardin.b2b.storage.StorageFileRevisionNotFoundException;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Controller for handling incoming backup requests from remote peers.
 */
@RestController
@RequestMapping("/api/peer")
@Slf4j
public class PeerController {

    @Autowired
    protected BackupSetRepository backupSetRepository;

    @Autowired
    protected BackupSourceRepository backupSourceRepository;

    @Autowired
    protected BackupTargetRepository backupTargetRepository;

    @Qualifier("gridFsStorageServiceFactory")
    @Autowired
    private StorageServiceFactory storageServiceFactory;


    /**
     * Handle a single file upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("computer-id") String computerId,
            @RequestParam("file") MultipartFile file
            ) throws B2BException, IOException {

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isBlank(originalFilename)) {
            return new ResponseEntity<>("MultipartFile.originalFilename cannot be blank",
                    new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        log.info("Single file upload from {}", computerId);
        log.debug("file.originalFilename = {}", originalFilename);

        //TODO check credentials for the remote computer

        // find or create the local BackupSet for the remote computer
        UUID remoteComputerUuid = UUID.fromString(computerId);
        BackupSet backupSet = getBackupSet(remoteComputerUuid);

        // store the file in the local storage
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        String revisionId = storageService.store(file.getInputStream(), originalFilename);
        log.debug("Successfully stored {} as {}", originalFilename, revisionId);

        return new ResponseEntity<>(revisionId, new HttpHeaders(), HttpStatus.OK);
    }

    @GetMapping("/download/{revisionId}")
    @ResponseBody
    public ResponseEntity<Resource> getItemContents(
            @PathVariable String revisionId,
            @RequestParam("computer-id") String computerId
    ) throws StorageFileRevisionNotFoundException, IOException, B2BException {

        UUID remoteComputerUuid = UUID.fromString(computerId);
        BackupSet backupSet = getBackupSet(remoteComputerUuid);
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        RevisionInfo revisionInfo = storageService.getRevisionInfo(revisionId);
        String filename = Paths.get(revisionInfo.getFilename()).getFileName().toString();
        Resource resource = storageService.getRevisionAsResource(revisionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + filename + "\"")
                .body(resource);
    }


    /**
     * Retrieve (or create if it doesn't exist) the BackupSet for the specified remote computer
     */
    private BackupSet getBackupSet(UUID computerId) throws B2BException {
        // try to find existing BackupSet with a PeerSource corresponding to this computer
        BackupSet backupSet = backupSetRepository.findAll().stream()
                .filter(s -> s.getBackupSource() instanceof PeerSource)
                .filter(s -> ((PeerSource) s.getBackupSource()).getRemoteComputerId().equals(computerId))
                //make sure we have at most 1 item and turn into an Optional
                .collect(MoreCollectors.toOptional())
                //if not present, create one
                .orElseGet(() -> createBackupSet(computerId))
                ;

        return backupSet;
    }

    /**
     * Create a {@link BackupSet} for the specified remote computer ID.
     * The BackupSet is configured with the default internal storage as target and a newly created {@link PeerSource}
     * as source.
     */
    private BackupSet createBackupSet(UUID computerId) {
        // find local target (or fail with exception)
/*
        LocalTarget localTarget = backupTargetRepository.findAll().stream()
                .filter(t -> t.getClass() == LocalTarget.class)
                .map(LocalTarget.class::cast)
                .findFirst()
                .orElseThrow(() -> new B2BException("No local destination configured"));
*/

        // create PeerSource for this computer
        PeerSource peerSource = backupSourceRepository.insert(new PeerSource(computerId));

        // create BackupSet
        BackupSet backupSet = new BackupSet();
        backupSet.setBackupSource(peerSource);
//        backupSet.setBackupTarget(localTarget);
        backupSetRepository.insert(backupSet);
        return backupSet;
    }



/*
    // Multiple file upload
    @PostMapping("/upload-multi")
    public ResponseEntity<?> uploadFileMulti(
            @RequestParam("extraField") String extraField,
            @RequestParam("files") MultipartFile[] uploadfiles) {

        logger.debug("Multiple file upload!");

        // Get file name
        String uploadedFileName = Arrays.stream(uploadfiles).map(MultipartFile::getOriginalFilename)
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity<>("please select a file!", HttpStatus.BAD_REQUEST);
        }

        try {

            saveUploadedFiles(Arrays.asList(uploadfiles));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Successfully uploaded - "
                + uploadedFileName, HttpStatus.OK);

    }
*/

/*
    // maps html form to a Model
    @PostMapping("/api/upload/multi/model")
    public ResponseEntity<?> multiUploadFileModel(@ModelAttribute UploadModel model) {

        logger.debug("Multiple file upload! With UploadModel");

        try {

            saveUploadedFiles(Arrays.asList(model.getFiles()));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity("Successfully uploaded!", HttpStatus.OK);

    }
*/
}