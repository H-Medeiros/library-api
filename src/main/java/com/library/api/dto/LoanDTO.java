package com.library.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanDTO {

    private Long id;

    @NotEmpty(message = "ISBN deve ser informado!")
    private String isbn;

    @NotEmpty(message = "Cliente deve ser informado!")
    private String customer;

    @NotEmpty(message = "E-mail deve ser informado!")
    private String email;

    private BookDTO book;
}
