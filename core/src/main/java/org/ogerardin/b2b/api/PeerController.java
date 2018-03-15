package org.ogerardin.b2b.api;

import lombok.extern.slf4j.Slf4j;
import org.ogerardin.b2b.B2BException;
import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.LocalTarget;
import org.ogerardin.b2b.domain.PeerSource;
import org.ogerardin.b2b.domain.mongorepository.BackupSetRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupSourceRepository;
import org.ogerardin.b2b.domain.mongorepository.BackupTargetRepository;
import org.ogerardin.b2b.storage.StorageService;
import org.ogerardin.b2b.storage.StorageServiceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
            @RequestParam("original-path") String originalPath,
            @RequestParam("file") MultipartFile file
            ) throws B2BException, IOException {

        log.debug("Single file upload from " + computerId);

/*
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }
*/

        //TODO we should also check credentials for the remote computer

        // find or create the local BackupSet for the remote computer
        UUID remoteComputerUuid = UUID.fromString(computerId);
        BackupSet backupSet = getBackupSet(remoteComputerUuid);

        // store the file in the local storage
        StorageService storageService = storageServiceFactory.getStorageService(backupSet.getId());
        storageService.store(file.getInputStream(), originalPath);

        return new ResponseEntity<>("Successfully uploaded '" + file.getOriginalFilename() +"'",
                new HttpHeaders(), HttpStatus.OK);

    }

    /**
     * Retrieve (or create if it doesn't exist) the BackupSet for the specified remote computer
     */
    private BackupSet getBackupSet(UUID computerId) throws B2BException {
        // try to find existing BackupSet with a PeerSource corresponding to this computer
        List<BackupSet> backupSets = backupSetRepository.findAll().stream()
                .filter(s -> s.getBackupSource() instanceof PeerSource)
                .filter(s -> ((PeerSource) s.getBackupSource()).getRemoteComputerId().equals(computerId))
                .collect(Collectors.toList());

        // if none was found, create one
        if (backupSets.isEmpty()) {
            return createBackupSet(computerId);
        }
        if (backupSets.size() > 1) {
            throw new B2BException("More than 1 BackupSet found for remote computer " + computerId);
        }
        return backupSets.get(0);
    }

    /**
     * Create a {@link BackupSet} for the specified remote computer ID.
     * The BackupSet is configured with the default internal storage as target and a newly created {@link PeerSource}
     * as source.
     */
    private BackupSet createBackupSet(UUID computerId) throws B2BException {
        // find local target (or fail with exception)
        LocalTarget localTarget = backupTargetRepository.findAll().stream()
                .filter(t -> t.getClass() == LocalTarget.class)
                .map(LocalTarget.class::cast)
                .findFirst()
                .orElseThrow(() -> new B2BException("No local destination configured"));

        // create PeerSource for this computer
        PeerSource peerSource = backupSourceRepository.insert(new PeerSource(computerId));

        // create BackupSet
        BackupSet backupSet = new BackupSet();
        backupSet.setBackupSource(peerSource);
        backupSet.setBackupTarget(localTarget);
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