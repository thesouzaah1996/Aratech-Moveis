package com.aratechmoveis.login.funcionario.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long idFuncionario;

    private String nomeFuncionario;

    private String emailPessoal;

    private String emailCorporativo;

    private String senha;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "perfis_funcionarios", joinColumns = @JoinColumn(name = "funcionario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis;

    private boolean primeiroLogin;

    private boolean ativo;
}
