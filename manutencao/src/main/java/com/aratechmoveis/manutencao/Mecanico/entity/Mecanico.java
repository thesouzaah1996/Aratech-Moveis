package com.aratechmoveis.manutencao.Mecanico.entity;

import com.aratechmoveis.manutencao.chamado.entity.Chamado;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "mecanicos", indexes = {
        @Index(name = "idx_mecanico_ativo", columnList = "ativo")
})
public class Mecanico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "O nome do mecânico é obrigatório")
    @Column(nullable = false, length = 150)
    private String nome;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mecanico_especialidades", joinColumns = @JoinColumn(name = "mecanico_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "especialidade", nullable = false, length = 30)
    @Builder.Default
    private List<Especialidade> especialidades = new java.util.ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Turno turno;

    @ToString.Exclude
    @OneToMany(mappedBy = "mecanico", fetch = FetchType.LAZY)
    private List<Chamado> chamados;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;
}
