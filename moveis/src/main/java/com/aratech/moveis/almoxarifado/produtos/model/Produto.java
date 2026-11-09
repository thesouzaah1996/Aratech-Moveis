package com.aratech.moveis.almoxarifado.produtos.model;

import com.aratech.moveis.almoxarifado.categorias.model.Categoria;
import com.aratech.moveis.almoxarifado.fornecedores.model.Fornecedor;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "produto",
        uniqueConstraints = @UniqueConstraint(name = "uk_produto_sku", columnNames = "sku"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 2, max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank
    @Pattern(regexp = "[A-Z0-9_-]{1,64}")
    @Column(nullable = false, length = 64, updatable = false)
    private String sku;

    @NotNull
    @Min(0)
    @Max(1000000000)
    @Column(nullable = false)
    private Integer quantidade;

    @Min(0)
    @Max(999999999)
    private Integer quantidadeMinima;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Fornecedor fornecedor;

    @NotBlank
    @Size(min = 2, max = 100)
    @Column(nullable = false, length = 100)
    private String localArmazenamento;

    @Size(max = 2000)
    @Column(length = 2000)
    private String descricao;

    private LocalDateTime vencimentoProduto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @PrePersist
    private void prepararCadastro() {
        if (criadoEm == null) criadoEm = LocalDateTime.now();
        if (sku != null) sku = sku.strip().toUpperCase(Locale.ROOT);
    }
}
