package com.library.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.api.dto.BookDTO;
import com.library.api.entity.Book;
import com.library.api.exception.BusinessException;
import com.library.api.service.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Optional;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = BookController.class)
public class BookControllerTest {

    static String BOOK_API = "/api/books";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("Deve criar um livro com sucesso")
    public void createBookTest() throws Exception {
        // Cenário
        BookDTO bookDTO = createNewBook();
        Book savedBook = Book.builder()
                .id(1L)
                .title("Titulo")
                .author("Author")
                .isbn("123456789")
                .build();

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willReturn(savedBook);

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar um erro de Validações quando não houver dados suficientes para a criação do livro")
    public void createInvalidBookTest() throws Exception {
        // Cenário
        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)));

    }

    @Test
    @DisplayName("Deve lançar um erro ao tentar cadastrar um livro com isbn já utilizado por outro")
    public void createBookWithDuplicatedIsbnTest() throws Exception {
        // Cenário
        BookDTO bookDTO = createNewBook();

        String json = new ObjectMapper().writeValueAsString(bookDTO);
        String errorMessage = "Isbn já cadastrado.";

        BDDMockito.given(bookService.save(Mockito.any(Book.class))).willThrow(new BusinessException(errorMessage));

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("errors", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("errors[0]").value(errorMessage));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception {
// Cenário
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title("Titulo")
                .author("Author")
                .isbn("123456789")
                .build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(book));

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(createNewBook().getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar Recource Not Found quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception {
        // Cenário
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // Execuçao
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat("/" + 1L))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception {
        // Cenário
        Book book = Book.builder().id(1L).build();
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.of(book));

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1L));

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar Resource Not Found quando não encontrar um livro para deletar")
    public void deleteInexistentBookTest() throws Exception {
        // Cenário
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete(BOOK_API.concat("/" + 1L));

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception {
        // Cenário
        Long id = 1L;
        String json = new ObjectMapper().writeValueAsString(createNewBook());

        Book updatingBook = Book.builder()
                .id(1L)
                .title("some title")
                .author("some author")
                .isbn("321")
                .build();

        Book updatedBook = Book.builder()
                .id(id)
                .title("Titulo")
                .author("Author")
                .isbn("123456789")
                .build();

        BDDMockito.given(bookService.getById(id)).willReturn(Optional.of(updatingBook));
        BDDMockito.given(bookService.update(updatingBook)).willReturn(updatedBook);

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);


        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("id").value(id))
                .andExpect(MockMvcResultMatchers.jsonPath("title").value(createNewBook().getTitle()))
                .andExpect(MockMvcResultMatchers.jsonPath("author").value(createNewBook().getAuthor()))
                .andExpect(MockMvcResultMatchers.jsonPath("isbn").value(123456789));

    }

    @Test
    @DisplayName("Deve retornar 404 ao tentar atualizar um livro inexistente")
    public void updateBookInexistentTest() throws Exception {
        // Cenário
        String json = new ObjectMapper().writeValueAsString(createNewBook());
        BDDMockito.given(bookService.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put(BOOK_API.concat("/" + 1L))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtrar livros")
    public void findBooksTest() throws Exception {
        // Cenário
        Long id = 1L;
        Book book = Book.builder()
                .id(id)
                .title(createNewBook().getTitle())
                .author(createNewBook().getAuthor())
                .isbn(createNewBook().getIsbn())
                .build();

        String queryString = String.format("?title=%s&author=%s&page=0&size=100", book.getTitle(), book.getAuthor());

        BDDMockito.given(bookService.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));

        // Execução
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // Validações
        mockMvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("content", Matchers.hasSize(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("totalElements").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageSize").value(100))
                .andExpect(MockMvcResultMatchers.jsonPath("pageable.pageNumber").value(0));
    }

    private BookDTO createNewBook() {
        return BookDTO.builder()
                .title("Titulo")
                .author("Author")
                .isbn("123456789")
                .build();
    }
}
