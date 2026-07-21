package com.aratechmoveis.almoxarifado.fornecedor.entity;

import com.aratechmoveis.almoxarifado.produto.entity.Produto;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "fornecedores", indexes = {
        @Index(name = "idx_fornecedor_ativo", columnList = "ativo")
})
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
    @Column(nullable = false, length = 20)
    private String telefone;

    private Representante representante;

    @ToString.Exclude
    @OneToMany(mappedBy = "fornecedor", fetch = FetchType.LAZY)
    private List<Produto> produto;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
