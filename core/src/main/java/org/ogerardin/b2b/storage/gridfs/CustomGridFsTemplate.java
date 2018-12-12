package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import org.bson.BsonObjectId;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.QueryMapper;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.Optional;

/**
 * This is a copy of {@link org.springframework.data.mongodb.gridfs.GridFsTemplate} just to allow customization of
 * the {@link GridFSBucket} returned by {@link #getGridFs()}.
 *
 * See https://jira.spring.io/browse/DATAMONGO-2165
 * See https://stackoverflow.com/questions/53728853/how-do-i-disable-gridfs-md5-calculation-in-spring-boot
 */
public class CustomGridFsTemplate {

    private static final String CONTENT_TYPE_FIELD = "_contentType";

    private final MongoDbFactory dbFactory;

    private final @Nullable
    String bucket;
    private final MongoConverter converter;
    private final QueryMapper queryMapper;

    private final GridFsBucketConfigurer gridFSBucketConfigurer;


    public CustomGridFsTemplate(MongoDbFactory dbFactory, MongoConverter converter, @Nullable String bucket) {
        this(dbFactory, converter, bucket, null);
    }
    public CustomGridFsTemplate(MongoDbFactory dbFactory, MongoConverter converter, @Nullable String bucket, GridFsBucketConfigurer gridFsBucketConfigurer) {

        Assert.notNull(dbFactory, "MongoDbFactory must not be null!");
        Assert.notNull(converter, "MongoConverter must not be null!");

        this.dbFactory = dbFactory;
        this.converter = converter;
        this.bucket = bucket;

        this.queryMapper = new QueryMapper(converter);
        this.gridFSBucketConfigurer = gridFsBucketConfigurer;
    }


    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String)
     */
    public ObjectId store(InputStream content, String filename) {
        return store(content, filename, (Object) null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.Object)
     */
    public ObjectId store(InputStream content, @Nullable Object metadata) {
        return store(content, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, com.mongodb.Document)
     */
    public ObjectId store(InputStream content, @Nullable Document metadata) {
        return store(content, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.String)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType) {
        return store(content, filename, contentType, (Object) null);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.Object)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable Object metadata) {
        return store(content, filename, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, java.lang.String, java.lang.Object)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType, @Nullable Object metadata) {

        Document document = null;

        if (metadata != null) {
            document = new Document();
            converter.write(metadata, document);
        }

        return store(content, filename, contentType, document);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, com.mongodb.Document)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable Document metadata) {
        return this.store(content, filename, null, metadata);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#store(java.io.InputStream, java.lang.String, com.mongodb.Document)
     */
    public ObjectId store(InputStream content, @Nullable String filename, @Nullable String contentType, @Nullable Document metadata) {

        Assert.notNull(content, "InputStream must not be null!");

        GridFSUploadOptions options = new GridFSUploadOptions();

        Document mData = new Document();

        if (StringUtils.hasText(contentType)) {
            mData.put(CONTENT_TYPE_FIELD, contentType);
        }

        if (metadata != null) {
            mData.putAll(metadata);
        }

        options.metadata(mData);

        return getGridFs().uploadFromStream(filename, content, options);
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.data.mongodb.gridfs.GridFsOperations#find(com.mongodb.Document)
     */
    public GridFSFindIterable find(Query query) {

        Assert.notNull(query, "Query must not be null!");

        Document queryObject = getMappedQuery(query.getQueryObject());
        Document sortObject = getMappedQuery(query.getSortObject());

        return getGridFs().find(queryObject).sort(sortObject);
    }

    public GridFSFile findOne(Query query) {
        return find(query).first();
    }

    public void delete(Query query) {
        for (GridFSFile x : find(query)) {
            getGridFs().delete(((BsonObjectId) x.getId()).getValue());
        }
    }


    private Document getMappedQuery(Document query) {
        return queryMapper.getMappedObject(query, Optional.empty());
    }

    private GridFSBucket getGridFs() {
        MongoDatabase db = dbFactory.getDb();
        GridFSBucket gridFSBucket = this.bucket == null ? GridFSBuckets.create(db) : GridFSBuckets.create(db, this.bucket);
        if (gridFSBucketConfigurer != null) {
            gridFSBucket = gridFSBucketConfigurer.configure(gridFSBucket);
        }
        return gridFSBucket;
    }


}
