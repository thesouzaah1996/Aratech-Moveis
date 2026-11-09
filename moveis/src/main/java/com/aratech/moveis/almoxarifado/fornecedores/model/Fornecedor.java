package com.aratech.moveis.almoxarifado.fornecedores.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import lombok.*;

import java.util.Locale;

@Entity
@Table(
        name = "fornecedor",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_fornecedor_nome", columnNames = "nome_normalizado"),
            @UniqueConstraint(name = "uk_fornecedor_email", columnNames = "email")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fornecedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "nome_normalizado", nullable = false, length = 450)
    @Setter(AccessLevel.NONE)
    private String nomeNormalizado;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String email;

    @NotBlank
    @Pattern(regexp = "\\d{10,11}")
    @Column(nullable = false, length = 11)
    private String telefone;

    @Embedded @NotNull @Valid private Representante representante;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @PrePersist
    @PreUpdate
    private void normalizar() {
        if (nome != null) {
            nome = nome.strip().replaceAll(" +", " ");
            nomeNormalizado = nome.toLowerCase(Locale.ROOT);
        }
        if (email != null) email = email.strip().toLowerCase(Locale.ROOT);
    }
}
