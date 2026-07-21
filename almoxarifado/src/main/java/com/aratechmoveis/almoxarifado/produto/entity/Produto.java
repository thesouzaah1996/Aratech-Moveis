package com.aratechmoveis.almoxarifado.produto.entity;

import com.aratechmoveis.almoxarifado.categoria.entity.Categoria;
import com.aratechmoveis.almoxarifado.fornecedor.entity.Fornecedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"categoria", "fornecedor"})
@Table(name = "produtos", indexes = {
        @Index(name = "idx_produto_categoria_id", columnList = "categoria_id"),
        @Index(name = "idx_produto_fornecedor_id", columnList = "fornecedor_id"),
        @Index(name = "idx_produto_local_armazenamento", columnList = "localArmazenamento")
})
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "Sku é obrigatório")
    @Column(unique = true, nullable = false, length = 50)
    private String sku;

    @Min(value = 0, message = "A quantidade não pode ser menor que zero")
    @Column(nullable = false)
    private Integer quantidade;

    @NotBlank(message = "Local de armazenamento é obrigatório")
    @Column(nullable = false, length = 150)
    private String localArmazenamento;

    @Column(length = 500)
    private String descricao;

    private LocalDateTime vencimentoProduto;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime criadoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;
}
