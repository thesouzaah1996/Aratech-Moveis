package com.aratech.moveis.assistencia.lotes.controller;

import com.aratech.moveis.assistencia.lotes.model.LoteAssistencia;
import com.aratech.moveis.assistencia.lotes.service.LoteAssistenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Assistência — Lotes e expedição",
        description =
                "A lista é ordenada pela data de entrada e ID, mais antigos primeiro. Recepção"
                    + " exige origemId, código, produto e quantidade positiva; mesma origemId e"
                    + " conteúdo devolve o lote existente, conteúdo divergente retorna 409. Fluxo:"
                    + " Não iniciado → Em andamento → Finalizado. Carregar exige Finalizado,"
                    + " registra carregadoEm e mantém status Finalizado. Repetir transição"
                    + " incompatível retorna 409. Na interface, dataEntrada é apresentada como data"
                    + " de saída; produto/quantidade continuam necessários no contrato de recepção"
                    + " da administração.")
@RestController
@RequestMapping("/aratech/assistencia/lotes")
@RequiredArgsConstructor
public class LoteAssistenciaController {
    private final LoteAssistenciaService service;

    @Operation(
            summary = "Listar lotes por antiguidade",
            description =
                    "Mais antigos primeiro (dataEntrada e ID). Aceita ASSISTENCIA_LOTES.CONSULTAR"
                            + " ou CARREGAMENTO_ASSISTENCIA.CONSULTAR (ou permissões amplas"
                            + " correspondentes). A ordenação visual de carregamento por status é"
                            + " aplicada pelo frontend.")
    @GetMapping
    public List<LoteAssistencia> listar() {
        return service.listar();
    }

    @Operation(
            summary = "Receber lote da administração",
            description =
                    "Exige ADMINISTRACAO_LOTES.CADASTRAR (ou permissão ampla), apesar da URL de"
                            + " assistência. origemId é a referência idempotente; reutilização com"
                            + " conteúdo diferente retorna 409. Estado e datas são definidos no"
                            + " servidor; campos de resposta não permitem forçar transições.")
    @PostMapping
    public LoteAssistencia receber(@RequestBody LoteAssistencia entrada) {
        return service.receber(entrada);
    }

    @Operation(
            summary = "Iniciar execução do lote",
            description =
                    "Somente Não iniciado, ainda não carregado. Define Em andamento e iniciadoEm.")
    @PostMapping("/{id}/iniciar")
    public LoteAssistencia iniciar(@PathVariable Long id) {
        return service.avancar(id, "iniciar");
    }

    @Operation(
            summary = "Finalizar execução do lote",
            description =
                    "Somente Em andamento, ainda não carregado. Define Finalizado e finalizadoEm.")
    @PostMapping("/{id}/finalizar")
    public LoteAssistencia finalizar(@PathVariable Long id) {
        return service.avancar(id, "finalizar");
    }

    @Operation(
            summary = "Confirmar lote colocado no caminhão",
            description =
                    "Exige CARREGAMENTO_ASSISTENCIA.FINALIZAR (ou permissão ampla). Somente lote"
                        + " Finalizado ainda não carregado. Registra carregadoEm; não muda o status"
                        + " para um estado novo.")
    @PostMapping("/{id}/carregar")
    public LoteAssistencia carregar(@PathVariable Long id) {
        return service.avancar(id, "carregar");
    }
}
