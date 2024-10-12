package com.library.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {

    private Long id;

    @NotEmpty(message = "Título deve ser informado!")
    private String title;

    @NotEmpty(message = "Autor deve ser informado!")
    private String author;

    @NotEmpty(message = "ISBN deve ser informado!")
    private String isbn;
}
