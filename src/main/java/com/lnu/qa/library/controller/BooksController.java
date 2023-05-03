package com.lnu.qa.library.controller;

import com.lnu.qa.library.models.Book;
import com.lnu.qa.library.repository.BookNotFoundException;
import com.lnu.qa.library.repository.BooksRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/books")
@AllArgsConstructor
public class BooksController {

    private final BooksRepository booksRepository;

    @GetMapping
    public Set<Book> getBooks() {
        return booksRepository.getBooks();
    }

    @GetMapping("/name/{name}")
    public Book getBookByName(@PathVariable String name) {
        return booksRepository.getBookByName(name);
    }

    @PostMapping
    public Book returnBook(@RequestBody Book savedBook) {
        return booksRepository.returnBook(savedBook);
    }

    @PostMapping("/name/{name}")
    public Book replaceBook(@PathVariable String name, @RequestBody Book savedBook) {
        return booksRepository.replaceBook(name, savedBook);
    }

    @GetMapping("/author/{author}")
    public List<Book> getFiveBooksByAuthor(@PathVariable String author) {
        return booksRepository.getFiveBooksByAuthor(author);
    }

    @ExceptionHandler({BookNotFoundException.class})
    public ResponseEntity<String> handleException(BookNotFoundException exception) {
        return new ResponseEntity<>(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

}
