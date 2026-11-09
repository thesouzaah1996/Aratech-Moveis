package com.aratech.moveis.fabricacao.controller;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.shared.auditoria.annotation.AuditadoNoServico;
import com.aratech.moveis.shared.dto.Pagina;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@AuditadoNoServico
@RequestMapping("/aratech/administracao/guarda-roupas")
@Tag(name="Administração — Guarda-roupas",description="Catálogo de fichas técnicas com componentes, medidas finais em mm, bordas, veio, furações, atributos livres e fotos. Não calcula projeto estrutural ou otimização de chapas. PCP congela uma cópia da ficha ao criar cada lote. ADMINISTRACAO_GUARDA_ROUPAS separa consulta, cadastro e alteração; inativação é realizada por PUT, sem apagar histórico.")
public class GuardaRoupaController {
    private final FabricacaoService service;
    @GetMapping
    @Operation(summary="Listar guarda-roupas",description="Busca por nome e código, paginação de 1 a 100 itens por página, página inicial zero. Somente metadados e referência da capa; não retorna os bytes das fotos. Exige CONSULTAR.")
    public Pagina<ModeloResumo> listar(@RequestParam(defaultValue="0") int pagina,@RequestParam(defaultValue="12") int tamanho,@RequestParam(defaultValue="") String busca,@RequestParam(defaultValue="false") boolean ativos){return service.modelos(pagina,tamanho,busca,ativos);}
    @GetMapping("/{id}")
    @Operation(summary="Consultar ficha técnica completa",description="Retorna versão atual, componentes por unidade e referências de fotos ativas. Modelo inexistente retorna 404. Exige CONSULTAR.")
    public ModeloGuardaRoupa obter(@PathVariable long id){return service.modelo(id);}
    @PostMapping
    @Operation(summary="Cadastrar guarda-roupa e componentes",description="Exige CADASTRAR. Código único, medidas externas positivas de até 10000 mm e 1 a 200 componentes, com chaves UUID e códigos únicos por ficha. Painel de corte deve usar UN e quantidade inteira, comprimento/largura positivos até 10000 mm e espessura até 200 mm. Bordas selecionadas exigem fita. Fotos são enviadas após salvar. Versão inicial 0; código duplicado 409; validações 400. Retorna 200.")
    public ModeloGuardaRoupa criar(@Valid @RequestBody FichaGuardaRoupa ficha){return service.salvar(null,ficha);}
    @PutMapping("/{id}")
    @Operation(summary="Atualizar ficha ou inativar modelo",description="Exige ALTERAR e versão atual, sob bloqueio transacional. Envia a ficha completa; preserva UUID de componentes existentes. Versão antiga retorna 409. Não altera fichas já congeladas em lotes nem apaga fotos referenciadas por esses lotes.")
    public ModeloGuardaRoupa atualizar(@PathVariable long id,@Valid @RequestBody FichaGuardaRoupa ficha){return service.salvar(id,ficha);}
    @PostMapping(value="/{id}/fotos",consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary="Adicionar foto de capa ou componente",description="Exige ALTERAR. Multipart arquivo JPEG/PNG até 5 MB, versão atual e legenda de até 150 caracteres; itemChave é UUID do componente salvo, omitido para capa. Máximo de quatro fotos por item ou capa. Valida formato real, até 6000 pixels por lado e 16 megapixels; recodifica pixels removendo metadados. Gravação de foto incrementa a versão da ficha e retorna ficha atualizada. Imagem inválida 400; versão antiga 409; arquivo acima do limite HTTP 413.")
    public ModeloGuardaRoupa foto(@PathVariable long id,@RequestParam long versao,@RequestParam(required=false) UUID itemChave,@RequestParam(defaultValue="") String legenda,@RequestPart MultipartFile arquivo){return service.enviarFoto(id,itemChave,versao,legenda,arquivo);}
    @DeleteMapping("/{id}/fotos/{foto}")
    @Operation(summary="Retirar foto do catálogo atual",description="Exige ALTERAR e versão atual. Retira a referência da ficha atual, incrementa versão e mantém bytes para os lotes que já referenciam essa foto. Foto inexistente 404; versão antiga 409.")
    public ModeloGuardaRoupa retirar(@PathVariable long id,@PathVariable long foto,@RequestParam long versao){return service.retirarFoto(id,foto,versao);}
    @GetMapping(value="/{id}/fotos/{foto}",produces={MediaType.IMAGE_JPEG_VALUE,MediaType.IMAGE_PNG_VALUE})
    @Operation(summary="Visualizar foto autenticada da ficha",description="Exige CONSULTAR. Retorna JPEG ou PNG reprocessado, sem cache público e sem URL externa. Confere o vínculo entre foto e modelo; inexistente retorna 404. Fotos retiradas permanecem disponíveis para fichas históricas.")
    public ResponseEntity<byte[]> imagem(@PathVariable long id,@PathVariable long foto){return respostaFoto(service,id,foto);}
    static ResponseEntity<byte[]> respostaFoto(FabricacaoService service,long id,long foto){var f=service.foto(id,foto);return ResponseEntity.ok().cacheControl(CacheControl.noStore()).header("X-Content-Type-Options","nosniff").contentType(MediaType.parseMediaType(f.mime())).body(f.conteudo());}
}
