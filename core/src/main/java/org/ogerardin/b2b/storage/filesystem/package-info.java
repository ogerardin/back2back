/**
 * Implementation of {@link org.ogerardin.b2b.storage.StorageService} using only the filesystem
 * This implementation doesn't support versioning, i.e. every time a file is stored it overwrites any
 * existing version of that file.
 */
package org.ogerardin.b2b.storage.filesystem;