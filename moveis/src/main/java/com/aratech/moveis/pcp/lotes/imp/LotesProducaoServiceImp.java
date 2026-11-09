package com.aratech.moveis.pcp.lotes.imp;
import com.aratech.moveis.pcp.lotes.LotesProducaoService;

import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
import com.aratech.moveis.pcp.lotes.repository.LotesProducaoRepository;
import com.aratech.moveis.fabricacao.dto.ItemGuardaRoupa;
import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.almoxarifado.produtos.dto.MovimentacaoDTO;
import com.aratech.moveis.almoxarifado.produtos.service.ProdutoService;
import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class LotesProducaoServiceImp implements LotesProducaoService {
 final LotesProducaoRepository repo; final FabricacaoRepository modelos; final OperacaoService base; final ProdutoService estoque;
 public List<Setor> setores(){return repo.setores();}
 public List<OpcaoVariante> variantes(String busca){filtros(0,busca);return repo.variantes(busca.strip());}
 public Variante variante(long id){return repo.variante(id,false);}
 public List<Origem> origens(String busca){filtros(0,busca);return repo.origens(busca.strip());}
 public void exigirOrigemConcluida(long origem){for(long id:repo.lotesDaOrigem(origem))if(!lote(id).status().equals("CONCLUIDO"))throw new AuthException(409,"Conclua todos os setores dos lotes vinculados antes de concluir a ordem de origem.");}
 public void exigirOrigemSemLotes(long origem){if(!repo.lotesDaOrigem(origem).isEmpty())throw new AuthException(409,"A ordem tem lotes de produção vinculados e não pode ser cancelada por este fluxo.");}
 public void exigirPedidoSemLotes(long pedido){for(long id:repo.origensDoPedido(pedido))exigirOrigemSemLotes(id);}
 public void validarReprogramacao(long origem,String produto,int quantidade){String atual=repo.produtoDaOrigem(origem);long total=repo.quantidadePlanejada(origem);if(total>0&&(!atual.equals(produto.strip())||quantidade<total))throw new AuthException(409,"A reprogramação não pode trocar o produto nem reduzir a quantidade abaixo dos lotes vinculados.");}
 @Transactional public Variante salvar(Long id,FichaVariante d){
  var modelo=modelos.modelo(d.modeloId(),true);FabricacaoService.validarFicha(d.ficha());
  if(id!=null){var atual=repo.variante(id,true);base.versao(atual.versao(),d.versao());if(atual.dados().modeloId()!=d.modeloId())throw new AuthException(400,"A variante não pode mudar de modelo.");}
  else if(d.versao()!=0)throw new AuthException(400,"Nova variante usa versão zero.");
  if(!modelo.ficha().ativo()&&d.ativo())throw new AuthException(400,"O modelo deve estar ativo.");
  var itens=d.ficha().itens().stream().map(ItemGuardaRoupa::chave).collect(Collectors.toSet());var vistos=new HashSet<UUID>();var setores=setores().stream().collect(Collectors.toMap(Setor::id,s->s));
  for(var r:d.roteiro()){
   if(!itens.contains(r.itemChave())||!vistos.add(r.itemChave()))throw new AuthException(400,"Roteiro deve referenciar um único componente existente.");
   if(r.consumoSetorId()!=null&&!setores.containsKey(r.consumoSetorId()))throw new AuthException(400,"Setor de consumo inexistente.");
   if(r.produtoEstoqueId()!=null&&!repo.produtoAtivo(r.produtoEstoqueId()))throw new AuthException(400,"Selecione produto de estoque ativo.");
   for(var op:r.operacoes())if(!setores.containsKey(op.setorId())||!setores.get(op.setorId()).natureza().equals("OPERACAO"))throw new AuthException(400,"Operação exige setor operacional; separação é atribuída pelo consumo.");
  }
  long salvo=repo.salvar(id,d);base.auditar("ADMINISTRACAO_GUARDA_ROUPAS","SALVAR_VARIANTE","VARIANTE",salvo,d.codigo());return variante(salvo);
 }
 public Calculo calcular(Variante v,int quantidade){
  Map<Long,Setor> setores=setores().stream().collect(Collectors.toMap(Setor::id,s->s));var linhas=new LinkedHashMap<Long,List<Linha>>();var erros=new ArrayList<String>();
  var roteiros=v.dados().roteiro().stream().collect(Collectors.toMap(Roteiro::itemChave,r->r));
  for(var i:v.dados().ficha().itens()){
   var r=roteiros.get(i.chave());var total=i.quantidade().multiply(BigDecimal.valueOf(quantidade));
   if(r==null||r.consumoSetorId()==null||!setores.containsKey(r.consumoSetorId()))erros.add(i.codigo()+": consumo obrigatório sem setor responsável.");
   else {
    var s=setores.get(r.consumoSetorId());
    // Material consumption exists exactly once, regardless of its number of manufacturing operations.
    var quantidadeEstoque=total.multiply(r.fatorEstoque());
    if(s.exigeMedidas())validarMedidas(i,s,erros);
    linhas.computeIfAbsent(s.id(),k->new ArrayList<>()).add(new Linha(i.chave(),"CONSUMO",i.codigo(),i.nome(),i.unidade().name(),i.quantidade(),total,r.produtoEstoqueId(),s.exigeMedidas()?especificacao(i):null,"Separar / consumir material","",quantidadeEstoque,true));
    if(s.natureza().equals("SEPARACAO")&&r.produtoEstoqueId()==null)erros.add(i.codigo()+": vincule o insumo a um produto do estoque para confirmar seu consumo.");
    if(s.natureza().equals("SEPARACAO")&&(quantidadeEstoque.stripTrailingZeros().scale()>0||quantidadeEstoque.compareTo(new BigDecimal("1000000000"))>0))erros.add(i.codigo()+": configure a conversão exata para unidades inteiras do estoque; nenhuma quantidade será arredondada.");
   }
   if(r==null)continue;
   if(i.cortar()&&r.operacoes().stream().noneMatch(op->setores.containsKey(op.setorId())&&setores.get(op.setorId()).exigeMedidas()))erros.add(i.codigo()+": peça de corte sem operação atribuída a um setor com medidas.");
   for(var op:r.operacoes()){
    var s=setores.get(op.setorId());if(s==null){erros.add(i.codigo()+": setor de operação inexistente.");continue;}
    if(s.exigeMedidas())validarMedidas(i,s,erros);
    // Generic ficha may include other departments' notes. Only the chosen operation's instructions travel.
    ItemGuardaRoupa tecnica=especificacao(i);
    var destino=linhas.computeIfAbsent(s.id(),k->new ArrayList<>());
    boolean consome=destino.removeIf(l->l.itemChave().equals(i.chave())&&l.natureza().equals("CONSUMO"));
    destino.add(new Linha(i.chave(),"OPERACAO",i.codigo(),i.nome(),i.unidade().name(),i.quantidade(),total,null,tecnica,op.nome(),Objects.toString(op.instrucoes(),""),null,consome));
   }
  }
  return new Calculo(linhas.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(e->new Grupo(setores.get(e.getKey()),e.getValue())).toList(),erros);
 }
 private boolean positivo(BigDecimal n){return n!=null&&n.signum()>0;}
 private ItemGuardaRoupa especificacao(ItemGuardaRoupa i){return new ItemGuardaRoupa(i.chave(),i.codigo(),i.nome(),i.tipo(),i.grupo(),i.material(),i.acabamento(),i.unidade(),i.quantidade(),i.cortar(),i.comprimentoMm(),i.larguraMm(),i.espessuraMm(),i.veio(),i.bordas(),i.fitaBorda(),"","",Map.of());}
 private void validarMedidas(ItemGuardaRoupa i,Setor s,List<String> erros){
  if(!positivo(i.comprimentoMm())||!positivo(i.larguraMm())||!positivo(i.espessuraMm())||i.material()==null||i.material().isBlank()||i.acabamento()==null||i.acabamento().isBlank()){
   String erro=i.codigo()+": faltam medidas, material ou acabamento/cor para "+s.nome()+".";
   if(!erros.contains(erro))erros.add(erro);
  }
 }
 @Transactional public Lote criar(NovoLote d){return base.comando(d.chaveOperacao(),"PCP_LOTE_PRODUCAO",d,Lote.class,()->{
  if(d.previsao().isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))throw new AuthException(400,"Previsão não pode estar no passado.");
  var v=repo.variante(d.varianteId(),true);if(!v.dados().ativo()||!modelos.modelo(v.dados().modeloId(),false).ficha().ativo())throw new AuthException(400,"Modelo e variante devem estar ativos.");
  if(d.ordemOrigemId()!=null)repo.validarOrigem(d.ordemOrigemId(),d.quantidade());
  long id=repo.criar(d,v,calcular(v,d.quantidade()),base.autor().nome());base.auditar("PCP_LOTES","CRIAR","LOTE_PRODUCAO",id,"Revisão congelada "+v.versao());return lote(id);
 });}
 public Lote lote(long id){return montar(repo.registro(id,false));}
 @Transactional public Lote revisar(long id,long revisao){var r=repo.registro(id,true);if(!repo.ordens(id).isEmpty())throw new AuthException(409,"A primeira liberação congelou a revisão deste lote. Ordens posteriores usam a mesma ficha.");if(r.revisao()!=revisao)throw new AuthException(409,"A prévia mudou; atualize o lote antes de revisar.");var v=repo.variante(r.snapshot().id(),true);if(!v.dados().ativo())throw new AuthException(409,"A variante está inativa.");repo.revisar(id,v,calcular(v,r.quantidade()));base.auditar("PCP_LOTES","REVISAR_PREVIA","LOTE_PRODUCAO",id,"Revisão "+r.revisao()+" → "+v.versao());return lote(id);}
 private Lote montar(LotesProducaoRepository.Registro r){var ordens=repo.ordens(r.id());return new Lote(r.id(),"LP-"+r.id(),r.snapshot().dados().ficha().nome(),r.snapshot().dados().nome(),r.quantidade(),r.previsao(),r.revisao(),r.origem(),r.observacoes(),r.autor(),status(r.calculo(),ordens),r.calculo(),ordens);}
 public static String status(Calculo c,List<Ordem> ordens){if(!c.pendencias().isEmpty())return "BLOQUEADO";if(ordens.isEmpty())return "PLANEJADO";if(ordens.size()<c.setores().size())return "LIBERACAO_PARCIAL";return ordens.stream().allMatch(o->o.status().equals("CONCLUIDA"))?"CONCLUIDO":"EM_PRODUCAO";}
 public Pagina<Resumo> listar(int pagina,String busca){filtros(pagina,busca);var itens=repo.ids(pagina,busca.strip()).stream().map(this::lote).map(l->new Resumo(l.id(),l.produto(),l.variante(),l.quantidade(),l.previsao(),l.revisao(),l.status(),l.previa().setores().size()-l.ordens().size())).toList();return new Pagina<>(itens,repo.total(busca.strip()),pagina,12);}
 @Transactional public Lote liberar(long id,Liberar d){var r=repo.registro(id,true);if(r.revisao()!=d.revisao())throw new AuthException(409,"Revise a prévia atual antes de liberar.");if(!r.calculo().pendencias().isEmpty())throw new AuthException(409,"Corrija a variante e atualize a prévia antes da primeira liberação.");var ids=r.calculo().setores().stream().map(g->g.setor().id()).collect(Collectors.toSet());if(!ids.containsAll(d.setores()))throw new AuthException(400,"Selecione apenas setores com trabalho calculado.");for(var g:r.calculo().setores())if(d.setores().contains(g.setor().id()))repo.liberar(id,g);base.auditar("PCP_LOTES","LIBERAR","LOTE_PRODUCAO",id,d.setores().toString());return lote(id);}
 public Setor setor(String caminho,String acao){var s=setores().stream().filter(v->caminho.equals(v.caminho())||caminho.startsWith(v.caminho()+"/")).findFirst().orElseThrow(()->new AuthException(404,"Setor não encontrado."));if(!Permissoes.permite(base.autor(),s.recurso()+"."+acao))throw new AuthException(403,"Sem permissão para este setor.");return s;}
 public Pagina<Ordem> fila(Setor s,int pagina,String busca){filtros(pagina,busca);return new Pagina<>(repo.fila(s.id(),pagina,busca.strip()),repo.totalFila(s.id(),busca.strip()),pagina,12);}
 public Ordem ordem(Setor s,long id){return repo.ordem(id,s.id(),false);}
 @Transactional public Ordem acao(Setor s,long id,String acao,Acao d){var o=repo.ordem(id,s.id(),true);String destino=acao.equals("iniciar")?"EM_ANDAMENTO":"CONCLUIDA";if(o.status().equals(destino))return o;base.versao(o.versao(),d.versao());String esperado=acao.equals("iniciar")?"LIBERADA":"EM_ANDAMENTO";if(!o.status().equals(esperado))throw new AuthException(409,"Transição de ordem inválida.");if(destino.equals("CONCLUIDA")&&s.natureza().equals("SEPARACAO")&&o.linhas().stream().filter(l->l.natureza().equals("CONSUMO")).anyMatch(l->!o.consumosConfirmados().contains(l.itemChave())))throw new AuthException(409,"Confirme o consumo de cada material antes de concluir a separação.");repo.estado(id,destino);base.auditar(s.recurso(),destino,"ORDEM_SETOR",id,o.codigoLote());return ordem(s,id);}
 @Transactional public Ordem consumir(Setor s,long id,Consumo d){
  if(!s.natureza().equals("SEPARACAO"))throw new AuthException(400,"Este setor não confirma separação de estoque.");
  var o=repo.ordem(id,s.id(),true);var l=o.linhas().stream().filter(v->v.itemChave().equals(d.itemChave())&&v.natureza().equals("CONSUMO")).findFirst().orElseThrow(()->new AuthException(404,"Material não pertence a esta ordem."));
  if(o.consumosConfirmados().contains(d.itemChave()))return o;
  if(!o.status().equals("EM_ANDAMENTO"))throw new AuthException(409,"Inicie a separação antes de confirmar o consumo.");
  if(l.produtoEstoqueId()==null||l.quantidadeEstoque()==null||l.quantidadeEstoque().stripTrailingZeros().scale()>0)throw new AuthException(409,"O estoque exige conversão exata para unidades inteiras vinculadas ao produto.");
  estoque.saida(l.produtoEstoqueId(),MovimentacaoDTO.builder().quantidade(l.quantidadeEstoque()).chaveOperacao(d.chaveOperacao()).motivo("Consumo "+o.codigoLote()+" / OS-"+id+" / "+l.codigo()+": "+d.motivo()).build());repo.consumo(id,l,d.chaveOperacao());base.auditar(s.recurso(),"CONSUMIR","ORDEM_SETOR",id,l.codigo()+" "+l.total());return ordem(s,id);
 }
 private void filtros(int pagina,String busca){if(pagina<0||pagina>100000||busca==null||busca.length()>150)throw new AuthException(400,"Filtros inválidos.");}
}
