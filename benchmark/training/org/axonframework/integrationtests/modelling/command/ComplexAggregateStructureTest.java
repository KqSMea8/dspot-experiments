/**
 * Copyright (c) 2010-2018. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.axonframework.integrationtests.modelling.command;


import java.util.ArrayList;
import java.util.List;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventhandling.SimpleEventBus;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.modelling.command.EntityId;
import org.axonframework.modelling.command.TargetAggregateIdentifier;
import org.axonframework.modelling.command.inspection.AggregateModel;
import org.axonframework.modelling.command.inspection.AnnotatedAggregate;
import org.axonframework.modelling.command.inspection.AnnotatedAggregateMetaModelFactory;
import org.junit.Assert;
import org.junit.Test;


public class ComplexAggregateStructureTest {
    @Test
    public void testCommandsAreRoutedToCorrectEntity() throws Exception {
        AggregateModel<ComplexAggregateStructureTest.Book> bookAggregateModel = AnnotatedAggregateMetaModelFactory.inspectAggregate(ComplexAggregateStructureTest.Book.class);
        EventBus mockEventBus = SimpleEventBus.builder().build();
        mockEventBus.subscribe(( m) -> m.forEach(( i) -> System.out.println(i.getPayloadType().getName())));
        AnnotatedAggregate<ComplexAggregateStructureTest.Book> bookAggregate = AnnotatedAggregate.initialize(((ComplexAggregateStructureTest.Book) (null)), bookAggregateModel, mockEventBus);
        bookAggregate.handle(command(new ComplexAggregateStructureTest.CreateBookCommand("book1")));
        bookAggregate.handle(command(new ComplexAggregateStructureTest.CreatePageCommand("book1")));
        bookAggregate.handle(command(new ComplexAggregateStructureTest.CreateParagraphCommand("book1", 0)));
        bookAggregate.handle(command(new ComplexAggregateStructureTest.CreateParagraphCommand("book1", 0)));
        bookAggregate.handle(command(new ComplexAggregateStructureTest.UpdateParagraphCommand("book1", 0, 0, "Hello world")));
        bookAggregate.handle(command(new ComplexAggregateStructureTest.UpdateParagraphCommand("book1", 0, 1, "Hello world2")));
        Assert.assertEquals("Hello world", bookAggregate.getAggregateRoot().getPages().get(0).getParagraphs().get(0).getText());
        Assert.assertEquals("Hello world2", bookAggregate.getAggregateRoot().getPages().get(0).getParagraphs().get(1).getText());
    }

    public static class Book {
        @AggregateIdentifier
        private String bookId;

        @AggregateMember
        private List<ComplexAggregateStructureTest.Page> pages = new ArrayList<>();

        private int lastPage = -1;

        @CommandHandler
        public Book(ComplexAggregateStructureTest.CreateBookCommand cmd) {
            apply(new ComplexAggregateStructureTest.BookCreatedEvent(cmd.getBookId()));
        }

        @CommandHandler
        public void handle(ComplexAggregateStructureTest.CreatePageCommand cmd) {
            apply(new ComplexAggregateStructureTest.PageCreatedEvent(cmd.getBookId(), ((lastPage) + 1)));
        }

        @EventSourcingHandler
        protected void handle(ComplexAggregateStructureTest.BookCreatedEvent event) {
            this.bookId = event.getBookId();
        }

        @EventSourcingHandler
        protected void handle(ComplexAggregateStructureTest.PageCreatedEvent event) {
            this.lastPage = event.getPageId();
            pages.add(new ComplexAggregateStructureTest.Page(event.getPageId()));
        }

        public List<ComplexAggregateStructureTest.Page> getPages() {
            return pages;
        }

        public String getBookId() {
            return bookId;
        }
    }

    public static class Page {
        @EntityId
        private final int pageNumber;

        @AggregateMember
        private List<ComplexAggregateStructureTest.Paragraph> paragraphs = new ArrayList<>();

        private int lastParagraphId = -1;

        public Page(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        @CommandHandler
        public void handle(ComplexAggregateStructureTest.CreateParagraphCommand cmd) {
            apply(new ComplexAggregateStructureTest.ParagraphCreatedEvent(cmd.getBookId(), pageNumber, ((lastParagraphId) + 1)));
        }

        @EventSourcingHandler
        protected void handle(ComplexAggregateStructureTest.ParagraphCreatedEvent event) {
            System.out.println("Paragraph created in aggregate");
            this.lastParagraphId = event.getParagraphId();
            this.paragraphs.add(new ComplexAggregateStructureTest.Paragraph(event.getParagraphId()));
        }

        public List<ComplexAggregateStructureTest.Paragraph> getParagraphs() {
            return paragraphs;
        }

        public int getPageNumber() {
            return pageNumber;
        }
    }

    public static class Paragraph {
        @EntityId
        private final int paragraphId;

        private String text;

        public Paragraph(int paragraphId) {
            this.paragraphId = paragraphId;
        }

        @CommandHandler
        public void handle(ComplexAggregateStructureTest.UpdateParagraphCommand cmd) {
            isTrue(((cmd.getParagraphId()) == (paragraphId)), () -> "UpdatePageCommand reached the wrong paragraph");
            apply(new ComplexAggregateStructureTest.ParagraphUpdatedEvent(cmd.getBookId(), cmd.getPageNumber(), paragraphId, cmd.getText()));
        }

        @EventSourcingHandler
        public void handle(ComplexAggregateStructureTest.ParagraphUpdatedEvent event) {
            if ((event.getParagraphId()) == (paragraphId)) {
                this.text = event.getText();
            }
        }

        public int getParagraphId() {
            return paragraphId;
        }

        public String getText() {
            return text;
        }
    }

    public static class CreateBookCommand {
        private final String bookId;

        private CreateBookCommand(String bookId) {
            this.bookId = bookId;
        }

        public String getBookId() {
            return bookId;
        }
    }

    public static class BookCreatedEvent {
        private final String bookId;

        public BookCreatedEvent(String bookId) {
            this.bookId = bookId;
        }

        public String getBookId() {
            return bookId;
        }
    }

    public static class CreatePageCommand {
        @TargetAggregateIdentifier
        private final String bookId;

        private CreatePageCommand(String bookId) {
            this.bookId = bookId;
        }

        public String getBookId() {
            return bookId;
        }
    }

    public static class PageCreatedEvent {
        private final String bookId;

        private final int pageId;

        public PageCreatedEvent(String bookId, int pageId) {
            this.bookId = bookId;
            this.pageId = pageId;
        }

        public String getBookId() {
            return bookId;
        }

        public int getPageId() {
            return pageId;
        }
    }

    public static class CreateParagraphCommand {
        private final String bookId;

        private final int pageNumber;

        private CreateParagraphCommand(String bookId, int pageNumber) {
            this.bookId = bookId;
            this.pageNumber = pageNumber;
        }

        public String getBookId() {
            return bookId;
        }

        public int getPageNumber() {
            return pageNumber;
        }
    }

    public static class ParagraphCreatedEvent {
        private final String bookId;

        private final int pageNumber;

        private final int paragraphId;

        public ParagraphCreatedEvent(String bookId, int pageNumber, int paragraphId) {
            this.bookId = bookId;
            this.pageNumber = pageNumber;
            this.paragraphId = paragraphId;
        }

        public String getBookId() {
            return bookId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getParagraphId() {
            return paragraphId;
        }
    }

    public static class UpdateParagraphCommand {
        @TargetAggregateIdentifier
        private final String bookId;

        private final int pageNumber;

        private final int paragraphId;

        private final String text;

        private UpdateParagraphCommand(String bookId, int pageNumber, int paragraphId, String text) {
            this.bookId = bookId;
            this.pageNumber = pageNumber;
            this.paragraphId = paragraphId;
            this.text = text;
        }

        public String getBookId() {
            return bookId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getParagraphId() {
            return paragraphId;
        }

        public String getText() {
            return text;
        }
    }

    public static class ParagraphUpdatedEvent {
        private final String bookId;

        private final int pageNumber;

        private final int paragraphId;

        private final String text;

        public ParagraphUpdatedEvent(String bookId, int pageNumber, int paragraphId, String text) {
            this.bookId = bookId;
            this.pageNumber = pageNumber;
            this.paragraphId = paragraphId;
            this.text = text;
        }

        public String getBookId() {
            return bookId;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        public int getParagraphId() {
            return paragraphId;
        }

        public String getText() {
            return text;
        }
    }
}
