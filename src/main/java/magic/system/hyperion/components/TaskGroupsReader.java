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
package magic.system.hyperion.components;

import com.fasterxml.jackson.databind.JsonNode;
import magic.system.hyperion.components.interfaces.IChangeableDocument;
import magic.system.hyperion.exceptions.HyperionException;
import magic.system.hyperion.reader.INodeReader;
import magic.system.hyperion.reader.TaskGroupReader;

/**
 * Reader for the list of task group out of the document.
 *
 * @author Thomas Lehmann
 */
public class TaskGroupsReader implements INodeReader {
    /**
     * Document operations to allow to add things only.
     */
    private final IChangeableDocument document;

    /**
     * Initialize reader with document.
     *
     * @param initDocument document where to add the task groups.
     */
    public TaskGroupsReader(final IChangeableDocument initDocument) {
        this.document = initDocument;
    }

    @Override
    public void read(JsonNode node) throws HyperionException {
        final var iter = node.elements();
        while (iter.hasNext()) {
            // there should be nothing else but individual task group.
            new TaskGroupReader(this.document).read(iter.next());
        }
    }
}
