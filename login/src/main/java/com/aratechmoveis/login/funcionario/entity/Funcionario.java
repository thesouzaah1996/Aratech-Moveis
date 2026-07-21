package com.aratechmoveis.login.funcionario.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "funcionarios", indexes = {
        @Index(name = "idx_funcionarios_email_corporativo", columnList = "emailCorporativo", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true)
    private Long idFuncionario;

    @NotBlank(message = "O nome do funcionário é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    @Column(nullable = false, length = 150)
    private String nomeFuncionario;

    @NotBlank(message = "O e-mail corporativo é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    @Column(nullable = false, unique = true, length = 150)
    private String emailPessoal;

    @Size(max = 150, message = "O e-mail corporativo deve ter no máximo 150 caracteres.")
    @Column(unique = true, length = 150)
    private String emailCorporativo;

    @ToString.Exclude
    @JsonIgnore
    @Column(length = 255)
    private String senha;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "perfis_funcionarios", joinColumns = @JoinColumn(name = "funcionario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis;

    @Column(nullable = false)
    private boolean primeiroLogin = true;

    @Column(nullable = false)
    private boolean ativo;
}
