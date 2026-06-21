package com.aratechmoveis.almoxarifado.fornecedor.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "fornecedor")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do fornecedor não pode estar vazio.")
    @Size(min = 2, max = 100, message = "O nome precisa ter entre 2 e 100 caracteres.")
    @Column(length = 100, nullable = false)
    private String nome;

    @NotBlank(message = "Informe um e-mail válido para o fornecedor.")
    @Email(message = "O e-mail informado não tem um formato válido.")
    @Size(max = 150, message = "O e-mail não pode ultrapassar 150 caracteres.")
    @Column(length = 150, nullable = false, unique = true)
    private String email;

    @NotBlank(message = "O telefone fixo do fornecedor é obrigatório.")
    @Column(nullable = false)
    private String telefone;

    private Representante representante;

    private Boolean ativo;
}
