package org.ogerardin.b2b.domain.mongorepository;

import org.ogerardin.b2b.domain.BackupSet;
import org.ogerardin.b2b.domain.BackupSource;
import org.ogerardin.b2b.domain.BackupTarget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BackupSetRepository extends MongoRepository<BackupSet, String> {

    List<BackupSet> findByBackupSourceAndBackupTarget(BackupSource source, BackupTarget target);


/*
    // The following fails, see: https://jira.spring.io/browse/DATAMONGO-1070
    @Query("{ 'backupSource.$id' : { '$oid' : ?0} , 'backupTarget.$id' : { '$oid' : ?1} }")
    List<BackupSet> findByBackupSourceIdAndBackupTargetId(String sourceId, String targetId);
*/

/*
    // The following works but is a bit awkward
    // from https://stackoverflow.com/questions/33805567/spring-data-mongo-custom-repository-query-with-objectid
    @Query("?0")
    List<BackupSet> _query(DBObject query);

    default List<BackupSet> findBySourceAndTarget(String sourceId, String targetId) {
        DBObject queryObject = BasicDBObjectBuilder
                .start("backupSource.$id", new ObjectId(sourceId))
                .add("backupTarget.$id", new ObjectId(targetId))
                .get();

        return this._query(queryObject);
    }
*/
}
