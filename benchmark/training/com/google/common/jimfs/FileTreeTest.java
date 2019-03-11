/**
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.jimfs;


import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;


/**
 * Tests for {@link FileTree}.
 *
 * @author Colin Decker
 */
@RunWith(JUnit4.class)
public class FileTreeTest {
    /* Directory structure. Each file should have a unique name.

    /
      work/
        one/
          two/
            three/
          eleven
        four/
          five -> /foo
          six -> ../one
          loop -> ../four/loop
      foo/
        bar/
    $
      a/
        b/
          c/
     */
    /**
     * This path service is for unix-like paths, with the exception that it recognizes $ and ! as
     * roots in addition to /, allowing for up to three roots. When creating a
     * {@linkplain PathType#toUriPath URI path}, we prefix the path with / to differentiate between
     * a path like "$foo/bar" and one like "/$foo/bar". They would become "/$foo/bar" and
     * "//$foo/bar" respectively.
     */
    private final PathService pathService = PathServiceTest.fakePathService(new PathType(true, '/') {
        @Override
        public ParseResult parsePath(String path) {
            String root = null;
            if (path.matches("^[/$!].*")) {
                root = path.substring(0, 1);
                path = path.substring(1);
            }
            return new ParseResult(root, Splitter.on('/').omitEmptyStrings().split(path));
        }

        @Override
        public String toString(@Nullable
        String root, Iterable<String> names) {
            root = Strings.nullToEmpty(root);
            return root + (Joiner.on('/').join(names));
        }

        @Override
        public String toUriPath(String root, Iterable<String> names, boolean directory) {
            // need to add extra / to differentiate between paths "/$foo/bar" and "$foo/bar".
            return "/" + (toString(root, names));
        }

        @Override
        public ParseResult parseUriPath(String uriPath) {
            Preconditions.checkArgument(uriPath.matches("^/[/$!].*"), "uriPath (%s) must start with // or /$ or /!");
            return parsePath(uriPath.substring(1));// skip leading /

        }
    }, false);

    private FileTree fileTree;

    private File workingDirectory;

    private final Map<String, File> files = new HashMap<>();

    // absolute lookups
    @Test
    public void testLookup_root() throws IOException {
        assertExists(lookup("/"), "/", "/");
        assertExists(lookup("$"), "$", "$");
    }

    @Test
    public void testLookup_nonExistentRoot() throws IOException {
        try {
            lookup("!");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("!a");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
    }

    @Test
    public void testLookup_absolute() throws IOException {
        assertExists(lookup("/work"), "/", "work");
        assertExists(lookup("/work/one/two/three"), "two", "three");
        assertExists(lookup("$a"), "$", "a");
        assertExists(lookup("$a/b/c"), "b", "c");
    }

    @Test
    public void testLookup_absolute_notExists() throws IOException {
        try {
            lookup("/a/b");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("/work/one/foo/bar");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("$c/d");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("$a/b/c/d/e");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
    }

    @Test
    public void testLookup_absolute_parentExists() throws IOException {
        assertParentExists(lookup("/a"), "/");
        assertParentExists(lookup("/foo/baz"), "foo");
        assertParentExists(lookup("$c"), "$");
        assertParentExists(lookup("$a/b/c/d"), "c");
    }

    @Test
    public void testLookup_absolute_nonDirectoryIntermediateFile() throws IOException {
        try {
            lookup("/work/one/eleven/twelve");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("/work/one/eleven/twelve/thirteen/fourteen");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
    }

    @Test
    public void testLookup_absolute_intermediateSymlink() throws IOException {
        assertExists(lookup("/work/four/five/bar"), "foo", "bar");
        assertExists(lookup("/work/four/six/two/three"), "two", "three");
        // NOFOLLOW_LINKS doesn't affect intermediate symlinks
        assertExists(lookup("/work/four/five/bar", LinkOption.NOFOLLOW_LINKS), "foo", "bar");
        assertExists(lookup("/work/four/six/two/three", LinkOption.NOFOLLOW_LINKS), "two", "three");
    }

    @Test
    public void testLookup_absolute_intermediateSymlink_parentExists() throws IOException {
        assertParentExists(lookup("/work/four/five/baz"), "foo");
        assertParentExists(lookup("/work/four/six/baz"), "one");
    }

    @Test
    public void testLookup_absolute_finalSymlink() throws IOException {
        assertExists(lookup("/work/four/five"), "/", "foo");
        assertExists(lookup("/work/four/six"), "work", "one");
    }

    @Test
    public void testLookup_absolute_finalSymlink_nofollowLinks() throws IOException {
        assertExists(lookup("/work/four/five", LinkOption.NOFOLLOW_LINKS), "four", "five");
        assertExists(lookup("/work/four/six", LinkOption.NOFOLLOW_LINKS), "four", "six");
        assertExists(lookup("/work/four/loop", LinkOption.NOFOLLOW_LINKS), "four", "loop");
    }

    @Test
    public void testLookup_absolute_symlinkLoop() {
        try {
            lookup("/work/four/loop");
            Assert.fail();
        } catch (IOException expected) {
        }
        try {
            lookup("/work/four/loop/whatever");
            Assert.fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLookup_absolute_withDotsInPath() throws IOException {
        assertExists(lookup("/."), "/", "/");
        assertExists(lookup("/./././."), "/", "/");
        assertExists(lookup("/work/./one/./././two/three"), "two", "three");
        assertExists(lookup("/work/./one/./././two/././three"), "two", "three");
        assertExists(lookup("/work/./one/./././two/three/././."), "two", "three");
    }

    @Test
    public void testLookup_absolute_withDotDotsInPath() throws IOException {
        assertExists(lookup("/.."), "/", "/");
        assertExists(lookup("/../../.."), "/", "/");
        assertExists(lookup("/work/.."), "/", "/");
        assertExists(lookup("/work/../work/one/two/../two/three"), "two", "three");
        assertExists(lookup("/work/one/two/../../four/../one/two/three/../three"), "two", "three");
        assertExists(lookup("/work/one/two/three/../../two/three/.."), "one", "two");
        assertExists(lookup("/work/one/two/three/../../two/three/../.."), "work", "one");
    }

    @Test
    public void testLookup_absolute_withDotDotsInPath_afterSymlink() throws IOException {
        assertExists(lookup("/work/four/five/.."), "/", "/");
        assertExists(lookup("/work/four/six/.."), "/", "work");
    }

    // relative lookups
    @Test
    public void testLookup_relative() throws IOException {
        assertExists(lookup("one"), "work", "one");
        assertExists(lookup("one/two/three"), "two", "three");
    }

    @Test
    public void testLookup_relative_notExists() throws IOException {
        try {
            lookup("a/b");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("one/foo/bar");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
    }

    @Test
    public void testLookup_relative_parentExists() throws IOException {
        assertParentExists(lookup("a"), "work");
        assertParentExists(lookup("one/two/four"), "two");
    }

    @Test
    public void testLookup_relative_nonDirectoryIntermediateFile() throws IOException {
        try {
            lookup("one/eleven/twelve");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
        try {
            lookup("one/eleven/twelve/thirteen/fourteen");
            Assert.fail();
        } catch (NoSuchFileException expected) {
        }
    }

    @Test
    public void testLookup_relative_intermediateSymlink() throws IOException {
        assertExists(lookup("four/five/bar"), "foo", "bar");
        assertExists(lookup("four/six/two/three"), "two", "three");
        // NOFOLLOW_LINKS doesn't affect intermediate symlinks
        assertExists(lookup("four/five/bar", LinkOption.NOFOLLOW_LINKS), "foo", "bar");
        assertExists(lookup("four/six/two/three", LinkOption.NOFOLLOW_LINKS), "two", "three");
    }

    @Test
    public void testLookup_relative_intermediateSymlink_parentExists() throws IOException {
        assertParentExists(lookup("four/five/baz"), "foo");
        assertParentExists(lookup("four/six/baz"), "one");
    }

    @Test
    public void testLookup_relative_finalSymlink() throws IOException {
        assertExists(lookup("four/five"), "/", "foo");
        assertExists(lookup("four/six"), "work", "one");
    }

    @Test
    public void testLookup_relative_finalSymlink_nofollowLinks() throws IOException {
        assertExists(lookup("four/five", LinkOption.NOFOLLOW_LINKS), "four", "five");
        assertExists(lookup("four/six", LinkOption.NOFOLLOW_LINKS), "four", "six");
        assertExists(lookup("four/loop", LinkOption.NOFOLLOW_LINKS), "four", "loop");
    }

    @Test
    public void testLookup_relative_symlinkLoop() {
        try {
            lookup("four/loop");
            Assert.fail();
        } catch (IOException expected) {
        }
        try {
            lookup("four/loop/whatever");
            Assert.fail();
        } catch (IOException expected) {
        }
    }

    @Test
    public void testLookup_relative_emptyPath() throws IOException {
        assertExists(lookup(""), "/", "work");
    }

    @Test
    public void testLookup_relative_withDotsInPath() throws IOException {
        assertExists(lookup("."), "/", "work");
        assertExists(lookup("././."), "/", "work");
        assertExists(lookup("./one/./././two/three"), "two", "three");
        assertExists(lookup("./one/./././two/././three"), "two", "three");
        assertExists(lookup("./one/./././two/three/././."), "two", "three");
    }

    @Test
    public void testLookup_relative_withDotDotsInPath() throws IOException {
        assertExists(lookup(".."), "/", "/");
        assertExists(lookup("../../.."), "/", "/");
        assertExists(lookup("../work"), "/", "work");
        assertExists(lookup("../../work"), "/", "work");
        assertExists(lookup("../foo"), "/", "foo");
        assertExists(lookup("../work/one/two/../two/three"), "two", "three");
        assertExists(lookup("one/two/../../four/../one/two/three/../three"), "two", "three");
        assertExists(lookup("one/two/three/../../two/three/.."), "one", "two");
        assertExists(lookup("one/two/three/../../two/three/../.."), "work", "one");
    }

    @Test
    public void testLookup_relative_withDotDotsInPath_afterSymlink() throws IOException {
        assertExists(lookup("four/five/.."), "/", "/");
        assertExists(lookup("four/six/.."), "/", "work");
    }
}
