package com.aratech.moveis.embalagem.service.imp;
import com.aratech.moveis.embalagem.service.EmbalagemService;

import com.aratech.moveis.embalagem.dto.*;
import com.aratech.moveis.embalagem.repository.EmbalagemRepository;
import com.aratech.moveis.fabricacao.dto.LoteResumo;
import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmbalagemServiceImp implements EmbalagemService {
    private final EmbalagemRepository repo;private final FabricacaoRepository fabricacao;private final OperacaoService base;
    public List<Esteira> opcoesEsteiras(){return repo.esteiras("",true);}
    public List<PainelEsteira> painel(String busca,boolean ativas){validarBusca(busca);return repo.esteiras(busca.strip(),ativas).stream().map(e->{var trabalhando=repo.tarefas(e.id(),true);return new PainelEsteira(e,trabalhando.size(),trabalhando,repo.tarefas(e.id(),false),repo.futuras(e.id()));}).toList();}
    @Transactional
    public Esteira salvarEsteira(Long id,EsteiraEntrada dto){long salvo;if(id==null){if(dto.versao()!=0)throw erro(400,"Nova esteira deve usar versão zero.");salvo=repo.criarEsteira(dto);}else{var antes=repo.esteira(id,true);versao(antes.versao(),dto.versao());if(dto.capacidade()<repo.ocupadas(id))throw erro(409,"A capacidade não pode ser menor que as operações em execução ou pausadas.");if(!dto.ativo()&&repo.pendentes(id)>0)throw erro(409,"A esteira tem etapas pendentes. Conclua ou cancele os planejamentos antes de inativar.");repo.editarEsteira(id,dto);salvo=id;}base.auditar("EMBALAGEM_ESTEIRAS",id==null?"CRIAR":"ALTERAR","ESTEIRA",salvo,dto.codigo());return repo.esteira(salvo,false);}
    public Pagina<LoteResumo> origens(int pagina,int tamanho,String busca){validarPagina(pagina,tamanho,busca);return repo.origens(pagina,tamanho,busca.strip());}
    public Pagina<ResumoEmbalagem> lotes(int pagina,int tamanho,String busca,String status){validarPagina(pagina,tamanho,busca);if(!Set.of("","AGUARDANDO","EM_EMBALAGEM","PAUSADO","EMBALADO","CANCELADO").contains(status))throw erro(400,"Situação de embalagem inválida.");return repo.lotes(pagina,tamanho,busca.strip(),status);}
    public LoteEmbalagem lote(long id){return repo.lote(id,false);}
    @Transactional
    public LoteEmbalagem receber(NovoLoteEmbalagem dto){return base.comando(dto.chaveOperacao(),"EMBALAGEM_RECEBER",dto,LoteEmbalagem.class,()->{
        if(!dto.prontoParaEmbalagem())throw erro(400,"Confirme que o lote está pronto para embalagem após as etapas necessárias.");
        if(dto.previsao().isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))throw erro(400,"A previsão não pode estar no passado.");
        // Ordem fixa de bloqueio: esteiras por ID, depois origem/lote.
        for(long id:dto.etapas().stream().map(EtapaEntrada::esteiraId).distinct().sorted().toList())if(!repo.esteira(id,true).ativo())throw erro(409,"O roteiro contém uma esteira inativa.");
        var origem=fabricacao.lote(dto.origemId(),true);if(!origem.status().equals("CORTADO"))throw erro(409,"O corte do lote ainda não foi concluído.");if(repo.recebido(dto.origemId()))throw erro(409,"Este lote já foi recebido pela Embalagem.");
        long id=repo.criarLote(dto,origem);evento(id,null,"RECEBIDO","Origem CT-"+origem.id()+"; prontidão para embalagem confirmada pelo operador.");return lote(id);
    });}
    @Transactional
    public LoteEmbalagem cancelar(long id,ComandoEmbalagem dto){var lote=repo.lote(id,true);versao(lote.versao(),dto.versao());if(!lote.status().equals("AGUARDANDO"))throw erro(409,"Só é possível cancelar um lote antes de iniciar a primeira etapa.");String motivo=motivo(dto.motivo());repo.cancelarEtapas(id);repo.statusLote(id,"CANCELADO",null);evento(id,null,"CANCELADO",motivo);return lote(id);}
    @Transactional
    public LoteEmbalagem operar(long esteiraId,long etapaId,String acao,ComandoEmbalagem dto){
        var esteira=repo.esteira(esteiraId,true);var lote=repo.lote(repo.loteDaEtapa(etapaId),true);versao(lote.versao(),dto.versao());
        var etapa=lote.etapas().stream().filter(e->e.id()==etapaId&&e.esteiraId()==esteiraId).findFirst().orElseThrow(()->erro(404,"A etapa não pertence a esta esteira."));
        if(Set.of("CANCELADO","EMBALADO").contains(lote.status()))throw erro(409,"Este lote já foi encerrado.");
        if(!etapa.liberada())throw erro(409,"Conclua as etapas anteriores antes de executar esta operação.");
        String esperado=switch(acao){case "iniciar"->"AGUARDANDO";case "retomar"->"PAUSADA";case "pausar","concluir"->"EM_EXECUCAO";default->throw erro(400,"Operação inválida.");};
        if(!etapa.status().equals(esperado))throw erro(409,"A etapa mudou de situação. Atualize o painel.");
        if(Set.of("iniciar","retomar").contains(acao)&&!esteira.ativo())throw erro(409,"A esteira está inativa.");
        if(acao.equals("iniciar")&&repo.ocupadas(esteiraId)>=esteira.capacidade())throw erro(409,"A esteira está com a capacidade ocupada. Operações pausadas também reservam posição.");
        boolean ultima=etapa.sequencia()==lote.etapas().size();
        if(acao.equals("concluir")&&ultima&&(dto.volumes()==null||dto.volumes()<1||!dto.pecasConferidas()||!dto.protecaoAplicada()||!dto.identificacaoConferida()))throw erro(400,"Informe os volumes e confirme peças, proteção e identificação para concluir a embalagem.");
        String detalhe=acao.equals("pausar")?motivo(dto.motivo()):etapa.nome();
        var autor=base.autor();repo.operar(etapaId,acao,autor.nome(),detalhe);
        String status=acao.equals("pausar")?"PAUSADO":acao.equals("concluir")&&ultima?"EMBALADO":"EM_EMBALAGEM";
        repo.statusLote(lote.id(),status,status.equals("EMBALADO")?dto.volumes():null);
        if(status.equals("EMBALADO"))detalhe="Conferidos: peças, proteção e identificação. Volumes: "+dto.volumes();
        evento(lote.id(),etapaId,acao.toUpperCase(Locale.ROOT),detalhe);return lote(lote.id());
    }
    private void evento(long lote,Long etapa,String acao,String detalhe){var autor=base.autor();repo.evento(lote,etapa,acao,detalhe,autor.nome());base.auditar("EMBALAGEM",acao,"LOTE_EMBALAGEM",lote,detalhe);}
    private String motivo(String valor){if(valor==null||valor.strip().length()<5||valor.length()>500)throw erro(400,"Informe um motivo de 5 a 500 caracteres.");return valor.strip();}
    private void versao(long atual,long recebida){if(atual!=recebida)throw erro(409,"Este registro foi alterado. Atualize os dados antes de continuar.");}
    private void validarPagina(int p,int t,String b){validarBusca(b);if(p<0||t<1||t>100||(long)p*t>Integer.MAX_VALUE)throw erro(400,"Paginação inválida.");}
    private void validarBusca(String b){if(b==null||b.length()>150)throw erro(400,"A busca aceita até 150 caracteres.");}
    private AuthException erro(int status,String mensagem){return new AuthException(status,mensagem);}
}
