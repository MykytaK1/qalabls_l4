package com.lnu.qa.library.repository;

import com.lnu.qa.library.models.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BooksRepository {

    private final Set<Book> books = new HashSet<>();

    public Set<Book> getBooks() {
        return books;
    }

    public Book getBookByName(String name) {
        Book book = findBookByName(name);
        books.remove(book);
        return book;
    }

    public Book returnBook(Book savedBook) {
        books.add(savedBook);
        return savedBook;
    }

    private Book findBookByName(String name) {
        return books.stream()
                .filter(b -> b.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(String.format("Book with name [%s] was not found", name)));
    }


    public Book replaceBook(String retrievedBookName, Book savedBook) {
        savedBook.setId(UUID.randomUUID().toString());
        Book book = findBookByName(retrievedBookName);
        books.remove(book);
        books.add(savedBook);
        return book;
    }

    public List<Book> getFiveBooksByAuthor(String author) {
        List<Book> result = books.stream()
                .filter(b -> b.getAuthor().equals(author))
                .limit(5)
                .collect(Collectors.toList());
        if (result.isEmpty()) {
            throw new BookNotFoundException(String.format("No books with author [%s] were found", author));
        }
        return result;
    }


}
