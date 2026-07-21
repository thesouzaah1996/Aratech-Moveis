package com.aratechmoveis.login.funcionario.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "perfis")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Nome do perfil é obrigatório")
    @Size(max = 100, message = "O nome do perfil deve ter no máximo 100 caracteres.")
    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(nullable = false)
    private boolean ativo = true;
}
