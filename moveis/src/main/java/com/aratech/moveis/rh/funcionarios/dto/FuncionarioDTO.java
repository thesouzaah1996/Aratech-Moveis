package com.aratech.moveis.rh.funcionarios.dto;

import com.aratech.moveis.rh.funcionarios.enums.EstadoCivil;
import com.aratech.moveis.rh.funcionarios.enums.Sexo;
import com.aratech.moveis.rh.funcionarios.enums.TipoConta;
import com.aratech.moveis.rh.funcionarios.enums.TipoContrato;
import com.aratech.moveis.rh.funcionarios.enums.TipoFuncionario;
import com.aratech.moveis.rh.funcionarios.enums.UF;

import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FuncionarioDTO {

    @NotBlank(message = "O nome completo é obrigatório")
    @Size(min = 3, max = 150)
    private String nomeCompleto;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "O CPF deve conter 11 dígitos")
    private String cpf;

    @Size(max = 20)
    private String rg;

    @Pattern(regexp = "^\\d{11}$", message = "O PIS/NIT/NIS deve conter 11 dígitos")
    private String pisNitNis;

    @NotNull(message = "A data de nascimento é obrigatória")
    @Past(message = "A data de nascimento deve ser anterior à data atual")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataNascimento;

    @NotNull(message = "O sexo é obrigatório")
    private Sexo sexo;

    private EstadoCivil estadoCivil;

    @NotBlank(message = "O nome da mãe é obrigatório")
    @Size(min = 3, max = 150)
    private String nomeMae;

    @Size(max = 150)
    private String nomePai;

    @NotBlank(message = "O CEP é obrigatório")
    @Pattern(regexp = "\\d{8}", message = "O CEP deve conter 8 dígitos")
    private String cep;

    @NotBlank(message = "O logradouro é obrigatório")
    @Size(min = 3, max = 150)
    private String logradouro;

    @NotBlank(message = "O número é obrigatório")
    @Size(max = 15)
    private String numero;

    @Size(max = 100)
    private String complemento;

    @NotBlank(message = "O bairro é obrigatório")
    @Size(min = 2, max = 100)
    private String bairro;

    @NotBlank(message = "A cidade é obrigatória")
    @Size(min = 2, max = 100)
    private String cidade;

    @NotNull(message = "A UF é obrigatória")
    private UF uf;

    @Pattern(regexp = "^$|\\d{10}", message = "O telefone fixo deve conter 10 dígitos")
    private String telefoneFixo;

    @Pattern(regexp = "^$|\\d{11}", message = "O celular deve conter 11 dígitos")
    private String celularWhatsapp;

    @NotNull(message = "A data de admissão é obrigatória")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataAdmissao;

    @NotNull(message = "O tipo de contrato é obrigatório")
    private TipoContrato tipoContrato;

    @Min(value = 1, message = "A jornada semanal deve ser maior que zero")
    @Max(value = 44, message = "A jornada semanal não pode ultrapassar 44 horas")
    private Integer jornadaSemanal;

    @NotBlank(message = "O cargo é obrigatório")
    @Size(min = 2, max = 100)
    private String cargo;

    @NotNull(message = "O tipo de funcionário é obrigatório")
    private TipoFuncionario tipoFuncionario;

    @NotBlank(message = "O setor é obrigatório")
    @Size(min = 2, max = 100)
    private String setor;

    @NotNull(message = "O salário é obrigatório")
    @DecimalMin(value = "0.01", message = "O salário deve ser maior que zero")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal salario;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "Informe um e-mail válido")
    @Size(max = 150)
    private String email;

    @NotBlank(message = "O banco é obrigatório")
    @Size(max = 100)
    private String banco;

    @NotBlank(message = "A agência é obrigatória")
    @Pattern(regexp = "^[0-9]{1,5}(-[0-9Xx])?$", message = "Informe uma agência válida")
    private String agencia;

    @NotBlank(message = "A conta é obrigatória")
    @Pattern(regexp = "^[0-9]{1,12}(-[0-9Xx])?$", message = "Informe uma conta válida")
    private String conta;

    @NotNull(message = "O tipo de conta é obrigatório")
    private TipoConta tipoConta;

    @Size(max = 150, message = "A chave PIX deve ter no máximo 150 caracteres")
    private String chavePix;

    private MultipartFile rgOuCnh;

    private MultipartFile ctps;

    private MultipartFile aso;

    private MultipartFile comprovanteResidencia;

    private MultipartFile tituloEleitor;

    private MultipartFile reservista;

    private MultipartFile diplomaCertificado;

    private MultipartFile certidaoCasamento;

    private MultipartFile certidaoNascimento;
}
