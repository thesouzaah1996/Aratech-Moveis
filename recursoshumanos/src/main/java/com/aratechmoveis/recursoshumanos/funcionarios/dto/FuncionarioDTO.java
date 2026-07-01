package com.aratechmoveis.recursoshumanos.funcionarios.dto;

import com.aratechmoveis.recursoshumanos.funcionarios.entity.TipoFuncionario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FuncionarioDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank(message = "O nome completo é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome deve ter entre 2 e 150 caracteres.")
    private String nome;

    @NotBlank(message = "O CPF é obrigatório.")
    @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF inválido.")
    private String cpf;

    @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}-\\d{1}", message = "RG inválido.")
    private String rg;

    @Pattern(regexp = "\\d{3}\\.\\d{5}\\.\\d{2}-\\d{1}", message = "PIS/NIT/NIS inválido.")
    private String pis;

    @NotNull(message = "A data de nascimento é obrigatória.")
    @Past(message = "A data de nascimento deve ser no passado.")
    private LocalDate dataNascimento;

    @NotBlank(message = "O sexo é obrigatório.")
    @Pattern(regexp = "^[MFO]$", message = "Sexo deve ser M, F ou O.")
    private String sexo;

    @Size(max = 20, message = "Estado civil deve ter no máximo 20 caracteres.")
    private String estadoCivil;

    @NotBlank(message = "O nome da mãe é obrigatório.")
    @Size(min = 2, max = 150, message = "O nome da mãe deve ter entre 2 e 150 caracteres.")
    private String nomeMae;

    @Size(max = 150, message = "O nome do pai deve ter no máximo 150 caracteres.")
    private String nomePai;

    @NotBlank(message = "O CEP é obrigatório.")
    @Pattern(regexp = "\\d{5}-\\d{3}", message = "CEP inválido.")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório.")
    @Size(max = 200, message = "O logradouro deve ter no máximo 200 caracteres.")
    private String logradouro;

    @NotBlank(message = "O número é obrigatório.")
    @Size(max = 10, message = "O número deve ter no máximo 10 caracteres.")
    private String numero;

    @Size(max = 60, message = "O complemento deve ter no máximo 60 caracteres.")
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório.")
    @Size(max = 100, message = "O bairro deve ter no máximo 100 caracteres.")
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória.")
    @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres.")
    private String cidade;

    @NotBlank(message = "A UF é obrigatória.")
    @Pattern(regexp = "^[A-Z]{2}$", message = "UF deve ter 2 letras maiúsculas.")
    private String uf;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{4}-\\d{4}", message = "Telefone fixo inválido.")
    private String telefone;

    @Pattern(regexp = "\\(\\d{2}\\) \\d{5}-\\d{4}", message = "Celular inválido.")
    private String celular;

    @NotNull(message = "A data de admissão é obrigatória.")
    @PastOrPresent(message = "A data de admissão não pode ser futura.")
    private LocalDate dataAdmissao;

    @NotBlank(message = "O tipo de contrato é obrigatório.")
    @Size(max = 60, message = "O tipo de contrato deve ter no máximo 60 caracteres.")
    private String tipoContrato;

    @Min(value = 1, message = "A jornada semanal deve ser no mínimo 1 hora.")
    @Max(value = 60, message = "A jornada semanal deve ser no máximo 60 horas.")
    private Integer jornadaHoras;

    @NotBlank(message = "O cargo é obrigatório.")
    @Size(max = 100, message = "O cargo deve ter no máximo 100 caracteres.")
    private String cargo;

    @NotNull(message = "Tipo funcionário é obrigatório")
    private TipoFuncionario tipoFuncionario;

    @NotBlank(message = "O setor é obrigatório.")
    @Size(max = 100, message = "O setor deve ter no máximo 100 caracteres.")
    private String setor;

    @NotNull(message = "O salário é obrigatório.")
    @DecimalMin(value = "0.01", message = "O salário deve ser maior que zero.")
    @Digits(integer = 10, fraction = 2, message = "Salário inválido.")
    private BigDecimal salario;

    @NotBlank(message = "O e-mail corporativo é obrigatório.")
    @Email(message = "E-mail inválido.")
    @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
    private String email;

    @NotBlank(message = "O banco é obrigatório.")
    @Size(max = 100, message = "O banco deve ter no máximo 100 caracteres.")
    private String banco;

    @NotBlank(message = "A agência é obrigatória.")
    @Size(max = 10, message = "A agência deve ter no máximo 10 caracteres.")
    private String agencia;

    @NotBlank(message = "A conta é obrigatória.")
    @Size(max = 20, message = "A conta deve ter no máximo 20 caracteres.")
    private String conta;

    @NotBlank(message = "O tipo de conta é obrigatório.")
    @Pattern(regexp = "^(corrente|poupanca)$", message = "Tipo de conta deve ser 'corrente' ou 'poupanca'.")
    private String tipoConta;

    @Size(max = 100, message = "A chave PIX deve ter no máximo 100 caracteres.")
    private String pix;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean ativo;
}
