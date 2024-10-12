package com.library.api.service;

import com.library.api.entity.Book;
import com.library.api.exception.BusinessException;
import com.library.api.repository.BookRepository;
import com.library.api.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
public class BookServiceTest {

    private BookService bookService;

    @MockBean
    private BookRepository bookRepository;

    @BeforeEach
    public void setup() {
        this.bookService = new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest() {
        // cenario
        Book savingBook = savingBook();
        Book bookReturned = returnedBook();

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(false);
        Mockito.when(bookRepository.save(savingBook)).thenReturn(bookReturned);

        // execução
        Book savedBook = bookService.save(savingBook);

        // Validação
        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getTitle()).isEqualTo(savingBook.getTitle());
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo(savingBook.getAuthor());
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo(savingBook.getIsbn());
    }

    @Test
    @DisplayName("Deve lançar erro de negócio ao tentar salvar um livro com ISBN duplicado")
    public void shouldNotSaveABookWithDuplicatedIsbnTest() {
        // cenario
        Book savingBook = savingBook();

        Mockito.when(bookRepository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execução
        Throwable exception = Assertions.catchThrowable(() -> bookService.save(savingBook));

        // Validação
        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("ISBN já cadastrado. Por favor, revise o ISBN do livro ou contate a sua gerência.");

        Mockito.verify(bookRepository, Mockito.never()).save(savingBook);
    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getByIdTest() {
        // Cenário
        Long id = 1L;
        Book savingBook = savingBook();
        savingBook.setId(id);

        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.of(savingBook));

        // Execução
        Optional<Book> foundBook = bookService.getById(id);

        // Validações
        Assertions.assertThat(foundBook.isPresent()).isTrue();
        Assertions.assertThat(foundBook.get().getId()).isEqualTo(id);
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(savingBook.getAuthor());
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(savingBook.getTitle());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(savingBook.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar vazio ao obter um livro por id quando ele não existe na base")
    public void bookNotFoundByIdTest() {
        // Cenário
        Long id = 1L;
        Mockito.when(bookRepository.findById(id)).thenReturn(Optional.empty());

        // Execução
        Optional<Book> book = bookService.getById(id);

        // Validações
        Assertions.assertThat(book.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBooktest() {
        //Cenário
        Book book = Book.builder()
                .id(1L)
                .build();

        // Execução
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> bookService.delete(book));

        // Validações
        Mockito.verify(bookRepository, Mockito.times(1)).delete(book);
    }

    @Test
    @DisplayName("Deve ocorrer um erro ao tentar deletar um livro inexistente")
    public void deleteInvalidBookTest() {
        //Cenário
        Book book = new Book();

        // Execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.delete(book));

        // Validações
        Mockito.verify(bookRepository, Mockito.never()).delete(book);
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBooktest() {
        //Cenário
        Book savingBook = Book.builder()
                .id(1L)
                .build();

        Book saveBook = returnedBook();

        Mockito.when(bookRepository.save(savingBook)).thenReturn(saveBook);

        // Execução
        Book book = bookService.update(savingBook);

        // Validações
        Assertions.assertThat(book.getId()).isEqualTo(saveBook.getId());
        Assertions.assertThat(book.getTitle()).isEqualTo(saveBook.getTitle());
        Assertions.assertThat(book.getAuthor()).isEqualTo(saveBook.getAuthor());
        Assertions.assertThat(book.getIsbn()).isEqualTo(saveBook.getIsbn());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateInvalidBookTest() {
        //Cenário
        Book book = new Book();

        // Execução
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.update(book));

        // Validações
        Mockito.verify(bookRepository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve filtrar livros pelas propriedades")
    public void findBookTest() {
        //Cenário
        Book book = savingBook();
        List<Book> lista = Arrays.asList(book);

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);

        Mockito.when(bookRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        // Execução
        Page<Book> result = bookService.find(book, pageRequest);

        // Validações
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(result.getContent()).isEqualTo(lista);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Deve obter um livro pelo ISBN")
    public void getBookByIsbnTest() {
        // Cenário
        String isbn = "123456789";
        Mockito.when(bookRepository.findByIsbn(isbn))
                .thenReturn(Optional.of(Book.builder().id(1L).isbn(isbn).build()));

        // Execução
        Optional<Book> book = bookService.getBookByIsbn(isbn);

        // Validações
        Assertions.assertThat(book.isPresent()).isTrue();
        Assertions.assertThat(book.get().getId()).isEqualTo(1L);
        Assertions.assertThat(book.get().getIsbn()).isEqualTo(isbn);
    }

    private Book savingBook() {
        return Book.builder()
                .title("Titulo")
                .author("Autor")
                .isbn("123456789")
                .build();
    }

    public Book returnedBook() {
        return Book.builder()
                .id(1L)
                .title("Titulo")
                .author("Autor")
                .isbn("123456789")
                .build();
    }
}
