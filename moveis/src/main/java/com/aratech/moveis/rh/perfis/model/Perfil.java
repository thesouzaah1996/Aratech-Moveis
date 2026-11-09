package com.aratech.moveis.rh.perfis.model;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(
        name = "perfil_acesso",
        uniqueConstraints = @UniqueConstraint(name = "uk_perfil_nome", columnNames = "nome"))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;
}
