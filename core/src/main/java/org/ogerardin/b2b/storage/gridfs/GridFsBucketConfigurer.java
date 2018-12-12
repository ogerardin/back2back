package org.ogerardin.b2b.storage.gridfs;

import com.mongodb.client.gridfs.GridFSBucket;

public interface GridFsBucketConfigurer {

    GridFSBucket configure(GridFSBucket gridFSBucket);

}
