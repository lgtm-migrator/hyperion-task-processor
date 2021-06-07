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
package magic.system.hyperion.reader;

import magic.system.hyperion.components.DocumentParameters;
import magic.system.hyperion.generics.ListCollector;
import magic.system.hyperion.interfaces.IVariable;
import magic.system.hyperion.tools.Capabilities;
import magic.system.hyperion.tools.MessagesCollector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Testing class {@link DocumentReader}.
 *
 * @author Thomas Lehmann
 */
@DisplayName("Testing DocumentReader class")
@SuppressWarnings({"checkstyle:multiplestringliterals", "checkstyle:magicnumber"})
public class DocumentReaderTest {
    /**
     * If something takes longer than a minute here this would be a problem.
     */
    private static final int DEFAULT_TIMEOUT_TASKGROUP = 1;

    /**
     * Intention to have a quite complete document.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testReader() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-is-valid.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(2, document.getListOfTaskGroups().get(0).getListOfTasks().size());
    }

    /**
     * Testing a document with Groovy.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testGroovy() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-groovy.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(3, document.getListOfTaskGroups().get(0).getListOfTasks().size());
        final var tags = document.getListOfTaskGroups().get(0).getListOfTasks().get(2).getTags();
        assertEquals("tag support", tags.get(0));
        assertEquals("third example", tags.get(1));

        MessagesCollector.clear();
        document.run(getDefaultDocumentParameters());
        assertTrue(MessagesCollector.getMessages().contains("set variable default=hello world!"));
        assertTrue(MessagesCollector.getMessages().contains("set variable test2=this is a demo"));
    }

    /**
     * Testing a document with Unix shell.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    @EnabledOnOs({OS.LINUX, OS.MAC})
    public void testUnixShell() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-unix-shell.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(3, document.getListOfTaskGroups().get(0).getListOfTasks().size());
        final var tags = document.getListOfTaskGroups().get(0).getListOfTasks().get(2).getTags();
        assertEquals("tag support", tags.get(0));
        assertEquals("third example", tags.get(1));

        MessagesCollector.clear();
        document.run(getDefaultDocumentParameters());
        assertTrue(MessagesCollector.getMessages().contains("set variable default=hello world!"));
        assertTrue(MessagesCollector.getMessages().contains("set variable test2=this is a demo"));
    }

    /**
     * Intention to test reading of the model.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testReaderForJustAModel() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/valid-document-with-just-a-model.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals("this is a test model (main model)",
                document.getModel().getData().getString("description"));
        assertEquals(List.of("hello world 1", "hello world 2", "hello world 3").toString(),
                document.getModel().getData().getList("listOfMessages").toString());
        assertEquals("this is a sub model inside a list of the main model",
                document.getModel().getData().getList("listOfAnything")
                        .getAttributeList(1)
                        .getAttributeList("subModel1").getString("description"));
        assertEquals(List.of("entry 1", "entry 2", "entry 3").toString(),
                document.getModel().getData().getList("listOfAnything")
                        .getAttributeList(2)
                        .getList("subList").toString());
    }
    /**
     * Testing of a matrix run.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testMatrix() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-matrix.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document);

        final var collector = new ListCollector<IVariable>();
        document.getListOfTaskGroups().forEach(
                group -> group.getVariablePublisher().subscribe(collector));
        document.run(getDefaultDocumentParameters());

        final String strLineBreak = System.getProperty("os.name")
                .toLowerCase(Locale.getDefault()).contains("windows")? "\r\n": "\n";

        assertEquals("Groovy:the first run|hello world 1!|hello world 2!|hello world 3!".
                        replaceAll("\\|", strLineBreak), collector.get(0).getValue());
        assertEquals("JShell:the first run|hello world 1!|hello world 2!|hello world 3!".
                        replaceAll("\\|", strLineBreak), collector.get(1).getValue());
        assertEquals("Groovy:the second run|hello world 1!".replaceAll("\\|", strLineBreak),
                collector.get(2).getValue());
        assertEquals("JShell:the second run|hello world 1!".replaceAll("\\|", strLineBreak),
                collector.get(3).getValue());
    }

    @Test
    public void testDockerContainerTask() throws URISyntaxException {
        assumeTrue(Capabilities.hasDocker());

        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-docker-container.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(3, document.getListOfTaskGroups().get(0).getListOfTasks().size());
        final var tags = document.getListOfTaskGroups().get(0).getListOfTasks().get(2).getTags();
        assertEquals("tag support", tags.get(0));
        assertEquals("third example", tags.get(1));

        MessagesCollector.clear();
        document.run(getDefaultDocumentParameters());
        assertTrue(MessagesCollector.getMessages().contains("set variable default=hello world!"));
        assertTrue(MessagesCollector.getMessages().contains("set variable test2=this is a demo"));
    }

    @Test
    public void testReadHasFailed() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/valid-document-with-just-a-model.yml").toURI());
        // change path to one that does not exist.
        final var reader = new DocumentReader(Paths.get(path.toString() + "yml"));
        final var document = reader.read();
        assertNull(document);
    }

    @Test
    public void testReadWithUnknownField() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/invalid-document-with-unknown-field.yml").toURI());
        final var reader = new DocumentReader(path);
        MessagesCollector.clear();
        final var document = reader.read();
        assertNull(document);

        assertTrue(MessagesCollector.getMessages()
                .stream().anyMatch(line -> line.contains("Unknown field 'unknown'!")));
    }

    /**
     * Testing a document with Groovy.
     *
     * @throws URISyntaxException when loading of the document has failed.
     */
    @Test
    public void testTasksRunningInParallel() throws URISyntaxException {
        final var path = Paths.get(getClass().getResource(
                "/documents/document-with-tasks-running-in-parallel.yml").toURI());
        final var reader = new DocumentReader(path);
        final var document = reader.read();
        assertNotNull(document, "Document shouldn't be null");
        assertEquals(1, document.getListOfTaskGroups().size());
        assertEquals(3, document.getListOfTaskGroups().get(0).getListOfTasks().size());

        MessagesCollector.clear();
        document.run(getDefaultDocumentParameters());

        // the order is descending because (1 sleeps 3s, 2 sleeps 2s and 3 doesn't sleep)
        assertEquals("Variable with name 'default' is used 3 times!",
                MessagesCollector.getMessages().get(0));
        assertEquals("set variable default=hello world 3", MessagesCollector.getMessages().get(1));
        assertEquals("set variable default=hello world 2", MessagesCollector.getMessages().get(2));
        assertEquals("set variable default=hello world 1", MessagesCollector.getMessages().get(3));
    }

    /**
     * Provide default document parameters.
     *
     * @return default document parameters.
     */
    private static DocumentParameters getDefaultDocumentParameters() {
        return DocumentParameters.of(List.of(), DEFAULT_TIMEOUT_TASKGROUP);
    }
}