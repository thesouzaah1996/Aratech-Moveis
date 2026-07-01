package com.aratechmoveis.recursoshumanos.funcionarios.entity;

import com.aratechmoveis.recursoshumanos.perfil.entity.Perfil;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "funcionarios")
public class Funcionario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    @Column(nullable = false, length = 150)
    private String nome;

    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido.")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}-\\d{1}", message = "RG inválido.")
    @Column(length = 12)
    private String rg;

    @Pattern(regexp = "\\d{3}\\.\\d{5}\\.\\d{2}-\\d{1}", message = "PIS/NIT/NIS inválido.")
    @Column(length = 15)
    private String pis;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser no passado.")
    @Column(nullable = false)
    private LocalDate dataNascimento;

    @NotBlank(message = "O sexo é obrigatório.")
    @Pattern(regexp = "^[MFO]$", message = "Sexo deve ser M, F ou O.")
    @Column(nullable = false, length = 1)
    private String sexo;

    @Size(max = 20, message = "Estado civil deve ter no máximo 20 caracteres.")
    @Column(length = 20)
    private String estadoCivil;

    @NotBlank(message = "O nome da mãe é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome da mãe deve ter entre 2 e 150 caracteres.")
    @Column(nullable = false, length = 150)
    private String nomeMae;

    @Size(max = 150, message = "O nome do pai deve ter no máximo 150 caracteres.")
    @Column(length = 150)
    private String nomePai;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido.")
    @Column(nullable = false, length = 9)
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório.")
    @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres.")
    @Column(nullable = false, length = 200)
    private String logradouro;

    @NotBlank(message = "O número é obrigatório.")
    @Size(max = 10, message = "O número deve ter no máximo 10 caracteres.")
    @Column(nullable = false, length = 10)
    private String numero;

    @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres.")
    @Column(length = 60)
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String cidade;

    @NotBlank(message = "A UF é obrigatória.")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ter 2 letras maiúsculas.")
    @Column(nullable = false, length = 2)
    private String uf;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4}-\\d{4}", message = "Telefone fixo inválido.")
    @Column(length = 15)
    private String telefone;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Celular inválido.")
    @Column(length = 16)
    private String celular;

    @NotNull(message = "A data de admissão é obrigatória.")
    @PastOrPresent(message = "A data de admissão não pode ser futura.")
    @Column(nullable = false)
    private LocalDate dataAdmissao;

    @NotBlank(message = "O tipo de contrato é obrigatório.")
    @Size(max = 60, message = "O tipo de contrato deve ter no máximo 60 caracteres.")
    @Column(nullable = false, length = 60)
    private String tipoContrato;

    @Min(value = 1, message = "A jornada semanal deve ser no mínimo 1 hora.")
    @Max(value = 60, message = "A jornada semanal deve ser no máximo 60 horas.")
    @Column
    private Integer jornadaHoras;

    @NotBlank(message = "O cargo é obrigatório.")
    @Size(max = 100, message = "O cargo deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String cargo;

    @NotNull(message = "O tipo de funcionário é obrigatório")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoFuncionario tipoFuncionario;

    @NotBlank(message = "O setor é obrigatório.")
    @Size(max = 100, message = "O setor deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String setor;

    @NotNull(message = "O salário é obrigatório.")
    @DecimalMin(value = "0.01", message = "O salário deve ser maior que zero.")
    @Digits(integer = 10, fraction = 2, message = "Salário inválido.")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salario;

    @NotBlank(message = "O e-mail corporativo é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "O banco é obrigatório.")
    @Size(max = 100, message = "O banco deve ter no máximo 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String banco;

    @NotBlank(message = "A agência é obrigatória.")
    @Size(max = 10, message = "A agência deve ter no máximo 10 caracteres.")
    @Column(nullable = false, length = 10)
    private String agencia;

    @NotBlank(message = "A conta é obrigatória.")
    @Size(max = 20, message = "A conta deve ter no máximo 20 caracteres.")
    @Column(nullable = false, length = 20)
    private String conta;

    @NotBlank(message = "O tipo de conta é obrigatório.")
    @Pattern(regexp = "^(corrente|poupanca)$", message = "Tipo de conta deve ser 'corrente' ou 'poupanca'.")
    @Column(nullable = false, length = 10)
    private String tipoConta;

    @Size(max = 100, message = "A chave PIX deve ter no máximo 100 caracteres.")
    @Column(length = 100)
    private String pix;

    @Column(nullable = false)
    private boolean ativo = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "perfis_funcionarios", joinColumns = @JoinColumn(name = "funcionario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis;

    private String emailCorporativo;

    private String password;
}
