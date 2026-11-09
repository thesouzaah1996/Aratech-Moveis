package com.aratech.moveis.manutencao.fila.controller;

import com.aratech.moveis.manutencao.chamados.dto.Response;
import com.aratech.moveis.manutencao.fila.dto.AtribuicaoDTO;
import com.aratech.moveis.manutencao.fila.service.FilaChamadosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Manutenção — Fila de atendimento",
        description =
                "Ordenação por prioridade ALTA, MEDIA, BAIXA, seguida de abertura e ID crescentes."
                        + " Atribuição exige mecânico ativo e muda para EM_MANUTENCAO. Chamado"
                        + " concluído não pode ser reatribuído. Conclusão exige mecânico e estado"
                        + " EM_MANUTENCAO; repetir conclusão já concluída devolve o registro, sem"
                        + " duplicar a ação.")
@RestController
@RequestMapping("/aratech/manutencao/fila")
@RequiredArgsConstructor
public class FilaChamadosController {
    private final FilaChamadosService service;

    @Operation(
            summary = "Listar itens da fila",
            description =
                    "Consulta paginada com os filtros disponíveis nos parâmetros. Não altera"
                            + " dados.")
    @GetMapping
    public Response listar(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "8") int tamanho,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca) {
        return service.listar(pagina, tamanho, status, busca);
    }

    @Operation(
            summary = "Consultar item da fila por ID",
            description =
                    "Consulta o registro existente pelo identificador; recurso ausente retorna"
                            + " 404.")
    @GetMapping("/{id}")
    public Response buscar(@PathVariable Long id) {
        return service.buscar(id);
    }

    @Operation(
            summary = "Atribuir mecânico ao chamado",
            description =
                    "Recebe mecanicoId. Mecânico deve existir e estar ativo; chamado não pode estar"
                            + " CONCLUIDA. Define EM_MANUTENCAO e registra o primeiro início.")
    @PostMapping("/{id}/mecanico")
    public Response atribuir(@PathVariable Long id, @RequestBody AtribuicaoDTO dto) {
        return service.atribuir(id, dto);
    }

    @Operation(
            summary = "Concluir chamado em atendimento",
            description =
                    "Exige EM_MANUTENCAO com mecânico atribuído. Repetição de uma conclusão já"
                            + " efetuada é idempotente.")
    @PostMapping("/{id}/concluir")
    public Response concluir(@PathVariable Long id) {
        return service.concluir(id);
    }
}
