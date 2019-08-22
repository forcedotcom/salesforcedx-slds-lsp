/*
 * Copyright (c) 2018, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: BSD-3-Clause
 * For full license text, see the LICENSE file in the repo root or https://opensource.org/licenses/BSD-3-Clause
 */

package com.salesforce.slds.shared.utils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ResourceUtilities {

    public static List<String> getResources(Class reference, String resourceName) {
        List<String> files = new ArrayList<>();
        URL resource = reference.getProtectionDomain().getCodeSource().getLocation();

            try {
                if (resource.getProtocol().contentEquals("file") &&
                        resource.toString().endsWith(".jar") == false) {
                    Path root = Paths.get(resource.toURI());

                    Stream<Path> walk = Files.walk(root);
                    walk.filter(path -> path.toFile().isFile())
                            .map(path -> "/" + root.relativize(path).toString())
                            .filter(path -> path.contains(resourceName))
                            .forEachOrdered(path ->
                                    files.add(path)
                            );

                    return files;
                }


                ZipInputStream zip = new ZipInputStream(resource.openStream());

                ZipEntry zipEntry;

                while ((zipEntry = zip.getNextEntry()) != null) {
                    String name = "/" + zipEntry.getName();

                    if ((name.contains(resourceName))
                            && name.endsWith("/") == false) {
                        files.add(name.replace(SPRING_BOOT_CLASS + "/", ""));
                    }
                }

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            return files;
    }

    private static final String SPRING_BOOT_CLASS = "BOOT-INF/classes";
}

