package com.aratech.moveis.manutencao.mecanicos.model;

import com.aratech.moveis.manutencao.mecanicos.enums.EspecialidadeMecanico;
import com.aratech.moveis.manutencao.mecanicos.enums.TurnoMecanico;

import jakarta.persistence.*;

import lombok.*;

import java.util.*;

@Entity
@Table(name = "manutencao_mecanico")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Mecanico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TurnoMecanico turno;

    @Column(nullable = false)
    @Builder.Default
    private boolean ativo = true;

    @ElementCollection
    @CollectionTable(
            name = "manutencao_mecanico_especialidade",
            joinColumns = @JoinColumn(name = "mecanico_id"))
    @Column(name = "especialidade", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<EspecialidadeMecanico> especialidades = new LinkedHashSet<>();
}
