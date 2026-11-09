package com.aratech.moveis.almoxarifado.categorias.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Locale;

@Entity
@Table(
        name = "categoria",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_categoria_nome", columnNames = "nome_normalizado")
        })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da categoria é obrigatório")
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String nome;

    @NotNull(message = "O status da categoria é obrigatório")
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "nome_normalizado", nullable = false, length = 300)
    @lombok.Setter(lombok.AccessLevel.NONE)
    private String nomeNormalizado;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    private void normalizarNome() {
        if (nome != null) {
            nome = nome.strip().replaceAll(" +", " ");
            nomeNormalizado = nome.toLowerCase(Locale.ROOT);
        }
    }
}
