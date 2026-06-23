package com.aratechmoveis.almoxarifado.produto.modelo;

import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    private String nome;

    @NotBlank(message = "Sku é obrigatório")
    @Column(unique = true)
    private String sku;

    @Min(value = 0, message = "A quantidade não pose ser menor que zero")
    private Integer quantidade;

    @Min(value = 0, message = "A quantidade no estoque não pode ser menor que zero")
    private Integer quantidadeEstoque;

    @NotBlank(message = "Local de armazenamento é obrigatório")
    private String localArmazenamento;

    private String descricao;

    private LocalDateTime vencimentoProduto;

    private final LocalDateTime criadoEm = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", sku='" + sku + '\'' +
                ", quantidadeEstoque=" + quantidadeEstoque +
                ", descricao='" + descricao + '\'' +
                ", vencimentoProduto=" + vencimentoProduto +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
