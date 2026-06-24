package com.aratechmoveis.almoxarifado.produto.modelo;

import com.aratechmoveis.almoxarifado.categoria.modelo.Categoria;
import com.aratechmoveis.almoxarifado.fornecedor.model.Fornecedor;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @Min(value = 0, message = "A quantidade não pode ser menor que zero")
    private Integer quantidade;

    @NotBlank(message = "Local de armazenamento é obrigatório")
    private String localArmazenamento;

    private String descricao;

    private LocalDateTime vencimentoProduto;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime criadoEm;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", sku='" + sku + '\'' +
                ", quantidade=" + quantidade +
                ", descricao='" + descricao + '\'' +
                ", vencimentoProduto=" + vencimentoProduto +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
