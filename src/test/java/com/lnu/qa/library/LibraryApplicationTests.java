package com.lnu.qa.library;

import com.github.javafaker.Faker;
import com.lnu.qa.library.models.Book;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Maps;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LibraryApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void shouldReturnBooks() {
        //Given
        Book book_1 = generateBook();
        Book book_2 = generateBook();
        restTemplate.postForObject("/books", book_1, Book.class);
        restTemplate.postForObject("/books", book_2, Book.class);
        //When
        Book[] books = restTemplate.getForObject("/books", Book[].class);
        //Then
        Assert.assertTrue(Sets.newLinkedHashSet(books).containsAll(Lists.list(book_1, book_2)));
    }

    @Test
    public void shouldReturnBookByName() {
        //Given
        Book book_1 = generateBook();
        restTemplate.postForObject("/books", book_1, Book.class);
        //When
        Book actualBook = restTemplate.getForObject("/books/name/{name}", Book.class, Maps.newHashMap("name", book_1.getName()));
        //Then
        Assert.assertEquals(actualBook, book_1);
    }

    @Test
    public void shouldReturnNotFoundStatusIfBookIsNotPresent() {
        //When
        ResponseEntity<String> actualBook = restTemplate.getForEntity("/books/name/{name}", String.class, Maps.newHashMap("name", "random"));
        //Then
        Assert.assertEquals(actualBook.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnBookToTheLibrary() {
        //Given
        int initialBooks = restTemplate.getForObject("/books", Book[].class).length;
        Book book_1 = generateBook();
        restTemplate.postForObject("/books", book_1, Book.class);
        //When
        Book[] books = restTemplate.getForObject("/books", Book[].class);
        //Then
        Assert.assertTrue(Sets.newLinkedHashSet(books).contains(book_1));
        Assert.assertEquals(initialBooks, books.length - 1);
    }

    @Test
    public void shouldReplaceBook() {
        //Given
        Book book_1 = generateBook();
        restTemplate.postForObject("/books", book_1, Book.class);
        //When
        Book newBook = generateBook();
        Book bookFromLibrary = restTemplate.postForObject("/books/name/{name}", newBook, Book.class, Maps.newHashMap("name", book_1.getName()));
        Book[] books = restTemplate.getForObject("/books", Book[].class);

        //Then
        Assert.assertEquals(bookFromLibrary.getName(), book_1.getName());
        Assert.assertTrue(Sets.newLinkedHashSet(books).stream().filter(b -> b.getName().equals(newBook.getName())).findAny().isPresent());
        Assert.assertFalse(Sets.newLinkedHashSet(books).contains(bookFromLibrary));
    }

    @Test
    public void shouldReturnNotFoundStatusIfBookIsNotFoundForReplacement() {
        //When
        Book newBook = generateBook();
        ResponseEntity<String> bookFromLibrary = restTemplate.postForEntity("/books/name/{name}", newBook, String.class, Maps.newHashMap("name", "random"));
        //Then
        Assert.assertEquals(bookFromLibrary.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldReturnFiveBooksForAuthor() {
        //Given
        String author = "some_author";
        IntStream.range(0, 5).mapToObj(i -> generateBook(author)).forEach(b -> {
            restTemplate.postForObject("/books", b, Book.class);
        });
        //When
        Book[] booksFromLibrary = restTemplate.getForObject("/books/author/{name}", Book[].class, Maps.newHashMap("name", author));
        //Then
        Assert.assertEquals(booksFromLibrary.length, 5);
        Set<Book> actualBooks = Sets.newLinkedHashSet(booksFromLibrary);
        Assert.assertTrue(actualBooks.stream().map(Book::getAuthor).allMatch(author::equals));
    }

    @Test
    public void shouldReturnLessBooksIfFiveNotFoundForAuthor() {
        //Given
        String author = "some_another_author";
        IntStream.range(0, 2).mapToObj(i -> generateBook(author)).forEach(b -> {
            restTemplate.postForObject("/books", b, Book.class);
        });
        //When
        Book[] booksFromLibrary = restTemplate.getForObject("/books/author/{name}", Book[].class, Maps.newHashMap("name", author));
        //Then
        Assert.assertEquals(booksFromLibrary.length, 2);
        Set<Book> actualBooks = Sets.newLinkedHashSet(booksFromLibrary);
        Assert.assertTrue(actualBooks.stream().map(Book::getAuthor).allMatch(author::equals));
    }

    @Test
    public void shouldReturnOnlyFiveBooksIfStoredMoreForAuthor() {
        //Given
        String author = "some_author";
        IntStream.range(0, 10).mapToObj(i -> generateBook(author)).forEach(b -> {
            restTemplate.postForObject("/books", b, Book.class);
        });
        //When
        Book[] booksFromLibrary = restTemplate.getForObject("/books/author/{name}", Book[].class, Maps.newHashMap("name", author));
        //Then
        Assert.assertEquals(booksFromLibrary.length, 5);
        Set<Book> actualBooks = Sets.newLinkedHashSet(booksFromLibrary);
        Assert.assertTrue(actualBooks.stream().map(Book::getAuthor).allMatch(author::equals));
    }

    @Test
    public void shouldReturnNotFoundStatusIfBooksNotFoundForAuthor() {
        //When
        ResponseEntity<String> booksFromLibrary = restTemplate.getForEntity("/books/author/{name}", String.class, Maps.newHashMap("name", "rnd_author"));
        //Then
        Assert.assertEquals(booksFromLibrary.getStatusCode(), HttpStatus.NOT_FOUND);
    }


    private Book generateBook() {
        Faker instance = Faker.instance();
        com.github.javafaker.Book fb = instance.book();
        Book book = new Book();
        book.setId(UUID.randomUUID().toString());
        book.setName(fb.title());
        book.setAuthor(fb.author());
        book.setGenre(fb.genre());
        return book;
    }

    private Book generateBook(String author) {
        Book book = generateBook();
        book.setAuthor(author);
        return book;
    }


}
