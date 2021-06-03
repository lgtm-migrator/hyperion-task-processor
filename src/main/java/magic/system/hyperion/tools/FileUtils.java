/*
 * The MIT License
 *
 * Copyright 2021 Thomas Lehmann.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package magic.system.hyperion.tools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.UUID;

/**
 * File utility for test classes.
 *
 * @author Thomas Lehmann
 */
public final class FileUtils {
    /**
     * Should be never instantiated.
     */
    private FileUtils() {
        // Nothing to do here.
    }

    /**
     * Get Resource as absolute path.
     *
     * @param strName relative resource name and path as string.
     * @return absolute path to the ressource
     * @throws URISyntaxException when the URI cannot be resolved.
     */
    public static Path getResourcePath(final String strName) throws URISyntaxException {
        final var url = FileUtils.class.getResource(strName);
        return new File(url.toURI()).toPath();
    }

    /**
     * Create temporary script path. On Unix the script is adjusted for full permission.
     *
     * @param strPrefix prefix of file name
     * @param strFileExtension the expected file extension.
     * @return path of the file.
     * @throws IOException when creation has failed.
     */
    public static Path createTemporaryFile(final String strPrefix,
                                           final String strFileExtension) throws IOException {
        final Path temporaryScriptPath;

        if (Capabilities.isWindows()) {
            temporaryScriptPath = Files.createTempFile(
                    strPrefix,UUID.randomUUID() + strFileExtension);
        } else {
            final Set<PosixFilePermission> ownerWritable
                    = PosixFilePermissions.fromString("rwxrwxrwx");
            final FileAttribute<?> permissions
                    = PosixFilePermissions.asFileAttribute(ownerWritable);
            temporaryScriptPath = Files.createTempFile(
                    strPrefix,UUID.randomUUID() + strFileExtension, permissions);
        }

        return temporaryScriptPath;
    }
}