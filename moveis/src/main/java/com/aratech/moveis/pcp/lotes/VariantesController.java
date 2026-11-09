package com.aratech.moveis.pcp.lotes;

import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import com.aratech.moveis.fabricacao.dto.ModeloGuardaRoupa;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor @AuditadoNoServico
@RequestMapping("/aratech/administracao/guarda-roupas/variantes")
@Tag(name="Administração — Variantes e roteiros",description="Reutiliza ADMINISTRACAO_GUARDA_ROUPAS: CONSULTAR, CADASTRAR e ALTERAR. Ficha específica da variante com componentes e operações por setor. Um único setor de consumo por componente; múltiplas operações não duplicam consumo. Não altera revisões já congeladas em lotes.")
public class VariantesController {
 final LotesProducaoService service; final FabricacaoService fabricacao;
 final com.aratech.moveis.pcp.requisicoes.service.RequisicaoService requisicoes;
 @GetMapping("/materiais") @Operation(summary="Selecionar insumos ativos do estoque para a variante",description="ADMINISTRACAO_GUARDA_ROUPAS.CONSULTAR. Reutiliza opções de materiais das requisições existentes: ID, SKU, nome e saldo. Não permite movimentação de estoque.")
 public List<com.aratech.moveis.shared.dto.Opcao> materiais(){return requisicoes.materiais();}
 @GetMapping @Operation(summary="Listar variantes ativas do catálogo",description="CONSULTAR, até 100 por busca. Para editar inativas use o ID conhecido. Filtros inválidos 400.")
 public List<OpcaoVariante> listar(@RequestParam(defaultValue="")String busca){return service.variantes(busca);}
 @GetMapping("/setores") @Operation(summary="Consultar destinos disponíveis para o roteiro",description="CONSULTAR. Identificadores, permissões e requisitos de cada setor; não concede acesso às filas operacionais.")
 public List<Setor> setores(){return service.setores();}
 @GetMapping("/modelo/{id}") @Operation(summary="Copiar ficha base ao preparar variante",description="CONSULTAR. Reutiliza catálogo, medidas e componentes existentes; cópia editável da variante é salva separadamente. Inexistente 404.")
 public ModeloGuardaRoupa modelo(@PathVariable long id){return fabricacao.modelo(id);}
 @GetMapping("/{id}") @Operation(summary="Consultar ficha e roteiro da variante",description="CONSULTAR. Retorna versão atual; inexistente 404. Cada componente pode ter consumo e operações com instruções por setor.")
 public Variante obter(@PathVariable long id){return service.variante(id);}
 @PostMapping @Operation(summary="Cadastrar variante específica de modelo/cor",description="CADASTRAR. Ficha técnica válida, revisão inicial zero, código único por modelo. Sem inferência de setores por nome/tipo. Permite salvar roteiros incompletos para revisão; esses dados bloqueiam a liberação no PCP. Produto de estoque referenciado deve estar ativo. Validação 400; duplicidade 409. Retorna 200.")
 public Variante criar(@Valid @RequestBody FichaVariante d){return service.salvar(null,d);}
 @PutMapping("/{id}") @Operation(summary="Revisar variante sem modificar lotes existentes",description="ALTERAR e versão atual. Incrementa revisão sob bloqueio transacional. Mesmo modelo obrigatório; versão antiga ou código duplicado 409; dados inválidos 400; inexistente 404.")
 public Variante atualizar(@PathVariable long id,@Valid @RequestBody FichaVariante d){return service.salvar(id,d);}
}
