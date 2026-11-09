package com.aratech.moveis.rh.funcionarios.controller;

import com.aratech.moveis.rh.funcionarios.dto.FuncionarioDTO;
import com.aratech.moveis.rh.funcionarios.dto.Response;
import com.aratech.moveis.rh.funcionarios.service.FuncionarioService;
import com.aratech.moveis.security.service.GestaoAcessoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(
        name = "RH — Funcionários",
        description =
                "Cadastro e atualização usam multipart/form-data. CPF válido, PIS quando informado"
                    + " e e-mail não podem duplicar outro cadastro; CPF é imutável na atualização."
                    + " Nascimento deve ser anterior a hoje e admissão posterior ao nascimento."
                    + " Documentos obrigatórios: rgOuCnh, ctps, aso e comprovanteResidencia."
                    + " Arquivos PDF/JPG/PNG com nome validado, até 5 MB cada e 20 MB no conjunto;"
                    + " requisição até 21 MB. Desativação revoga sessões; o último administrador"
                    + " funcionário ativo é protegido. Alterar e-mail de login também exige"
                    + " autorização para gerenciar acessos. Listagens expõem resumo, sem"
                    + " documentos, salário ou conta bancária.")
@RestController
@RequestMapping("aratech/rh/funcionarios")
@RequiredArgsConstructor
public class FuncionarioController {

    private final FuncionarioService funcionarioService;
    private final GestaoAcessoService gestao;

    @Operation(
            summary = "Cadastrar funcionário",
            description =
                    "Valida os campos de entrada e persiste o novo cadastro. As regras de unicidade"
                            + " e referência do módulo continuam válidas.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response> addFuncionario(@ModelAttribute FuncionarioDTO funcionarioDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(funcionarioService.addFuncionario(funcionarioDTO));
    }

    @Operation(
            summary = "Listar funcionários",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response getAllFuncionarios(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {
        return funcionarioService.getAllFuncionarios(pagina, tamanho);
    }

    @Operation(
            summary = "Consultar funcionário por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response getFuncionarioById(@PathVariable Long id) {
        return funcionarioService.getFuncionarioById(id);
    }

    @Operation(
            summary = "Atualizar funcionário",
            description =
                    "Atualiza o cadastro existente. Identificadores imutáveis, referências e estado"
                        + " do registro são validados segundo as regras do módulo. Quando a conta"
                        + " usa o e-mail cadastrado como login e não tem login corporativo"
                        + " separado, o novo e-mail deve respeitar nome@aratechmoveis; formato"
                        + " inválido retorna 400. E-mail pessoal com login corporativo separado"
                        + " mantém sua validação própria.")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Response updateFuncionario(@ModelAttribute FuncionarioDTO dto, @PathVariable Long id) {
        gestao.validarAlteracaoFuncionario(id, false);
        gestao.validarMudancaEmail(id, dto.getEmail());
        return funcionarioService.updateFuncionario(dto, id);
    }

    @Operation(
            summary = "Ativar funcionário",
            description = "Ativa o cadastro existente, respeitando as regras e vínculos do módulo.")
    @PatchMapping("/{id}/ativar")
    public Response enableEmployee(@PathVariable Long id) {
        gestao.validarAlteracaoFuncionario(id, false);
        return funcionarioService.enableEmployee(id);
    }

    @Operation(
            summary = "Desativar funcionário",
            description =
                    "Desativa sem excluir o histórico e respeita as regras de vínculos do módulo.")
    @PatchMapping("/{id}/desativar")
    public Response disableEmployee(@PathVariable Long id) {
        gestao.validarAlteracaoFuncionario(id, true);
        return funcionarioService.disableEmployee(id);
    }

    @InitBinder
    public void configurarCampos(WebDataBinder binder) {
        binder.setAllowedFields(
                "nomeCompleto",
                "cpf",
                "rg",
                "pisNitNis",
                "dataNascimento",
                "sexo",
                "estadoCivil",
                "nomeMae",
                "nomePai",
                "cep",
                "logradouro",
                "numero",
                "complemento",
                "bairro",
                "cidade",
                "uf",
                "telefoneFixo",
                "celularWhatsapp",
                "dataAdmissao",
                "tipoContrato",
                "jornadaSemanal",
                "cargo",
                "tipoFuncionario",
                "setor",
                "salario",
                "email",
                "banco",
                "agencia",
                "conta",
                "tipoConta",
                "chavePix",
                "rgOuCnh",
                "ctps",
                "aso",
                "comprovanteResidencia",
                "tituloEleitor",
                "reservista",
                "diplomaCertificado",
                "certidaoCasamento",
                "certidaoNascimento");
        binder.setAutoGrowNestedPaths(false);
    }
}
