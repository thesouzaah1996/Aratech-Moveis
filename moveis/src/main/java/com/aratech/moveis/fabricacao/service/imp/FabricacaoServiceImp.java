package com.aratech.moveis.fabricacao.service.imp;
import com.aratech.moveis.fabricacao.service.FabricacaoService;

import com.aratech.moveis.fabricacao.dto.*;
import static com.aratech.moveis.fabricacao.validation.FabricacaoValidator.*;
import com.aratech.moveis.fabricacao.repository.FabricacaoRepository;
import com.aratech.moveis.security.exception.AuthException;
import com.aratech.moveis.shared.dto.Pagina;
import com.aratech.moveis.shared.operacao.service.OperacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.io.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FabricacaoServiceImp implements FabricacaoService {
    private final FabricacaoRepository repo;
    private final OperacaoService base;

    public Pagina<ModeloResumo> modelos(int pagina,int tamanho,String busca,boolean ativos) {
        validarPagina(pagina,tamanho,busca);return repo.modelos(pagina,tamanho,busca.strip(),ativos);
    }
    public ModeloGuardaRoupa modelo(long id) { return repo.modelo(id,false); }
    @Transactional
    public ModeloGuardaRoupa salvar(Long id,FichaGuardaRoupa ficha) {
        validarFicha(ficha);
        long salvo;
        if(id==null) {
            if(ficha.versao()!=0)throw new AuthException(400,"Um novo modelo deve usar versão zero.");
            salvo=repo.criar(ficha);
        } else {
            var anterior=repo.modelo(id,true);versao(anterior.versao(),ficha.versao());
            repo.atualizar(id,ficha);salvo=id;
        }
        base.auditar("ADMINISTRACAO_GUARDA_ROUPAS",id==null?"CRIAR":"ALTERAR","GUARDA_ROUPA",salvo,ficha.codigo());
        return modelo(salvo);
    }


    @Transactional
    public ModeloGuardaRoupa enviarFoto(long id,UUID item,long versao,String legenda,MultipartFile arquivo) {
        var modelo=repo.modelo(id,true);versao(modelo.versao(),versao);
        if(item!=null&&modelo.ficha().itens().stream().noneMatch(i->i.chave().equals(item)))
            throw new AuthException(400,"Salve o componente na ficha antes de enviar suas fotos.");
        if(legenda==null||legenda.length()>150)throw new AuthException(400,"A legenda aceita até 150 caracteres.");
        if(modelo.fotos().stream().filter(f->Objects.equals(f.itemChave(),item)).count()>=4)
            throw new AuthException(400,"Cada item e a capa podem ter até quatro fotos.");
        var foto=prepararFoto(arquivo);
        repo.inserirFoto(id,item,legenda.strip(),foto.mime(),foto.conteudo());repo.incrementarVersao(id);
        base.auditar("ADMINISTRACAO_GUARDA_ROUPAS","FOTO_ADICIONADA","GUARDA_ROUPA",id,item==null?"Capa":item.toString());
        return modelo(id);
    }

    @Transactional
    public ModeloGuardaRoupa retirarFoto(long id,long foto,long versao) {
        var modelo=repo.modelo(id,true);versao(modelo.versao(),versao);
        repo.retirarFoto(id,foto);repo.incrementarVersao(id);
        base.auditar("ADMINISTRACAO_GUARDA_ROUPAS","FOTO_RETIRADA","GUARDA_ROUPA",id,Long.toString(foto));
        return modelo(id);
    }
    public FabricacaoRepository.FotoConteudo foto(long modelo,long foto){return repo.foto(modelo,foto);}
    public Pagina<LoteResumo> lotes(int pagina,int tamanho,String busca,boolean corte,String status) {
        validarPagina(pagina,tamanho,busca);
        if(!Set.of("","PLANEJADO","ENVIADO_CORTE","EM_CORTE","CORTADO","CANCELADO").contains(status))
            throw new AuthException(400,"Situação de lote inválida.");
        return repo.lotes(pagina,tamanho,busca.strip(),corte,status);
    }
    public LoteCorte lote(long id,boolean corte) {
        var lote=repo.lote(id,false);
        if(corte&&!Set.of("ENVIADO_CORTE","EM_CORTE","CORTADO").contains(lote.status()))
            throw new AuthException(404,"Este lote ainda não está disponível para o Corte.");
        return lote;
    }
    @Transactional
    public LoteCorte criarLote(NovoLoteCorte dto) {
        return base.comando(dto.chaveOperacao(),"FABRICACAO_LOTE",dto,LoteCorte.class,()->{
            if(dto.previsao().isBefore(LocalDate.now(ZoneId.of("America/Sao_Paulo"))))
                throw new AuthException(400,"A previsão de um novo lote não pode estar no passado.");
            var modelo=repo.modelo(dto.modeloId(),true);
            if(!modelo.ficha().ativo())throw new AuthException(400,"Selecione um guarda-roupa ativo.");
            validarFicha(modelo.ficha());
            if(modelo.ficha().itens().stream().noneMatch(ItemGuardaRoupa::cortar))
                throw new AuthException(400,"A ficha precisa ter pelo menos um painel para corte.");
            var autor=base.autor();long id=repo.criarLote(dto,modelo,autor.id(),autor.nome());
            base.auditar("PCP_LOTES","CRIAR","LOTE_CORTE",id,"Ficha "+modelo.id()+" revisão "+modelo.versao());
            return lote(id,false);
        });
    }
    @Transactional
    public LoteCorte transicao(long id,long versao,String acao) {
        var lote=repo.lote(id,true);versao(lote.versao(),versao);
        String esperado=switch(acao){case "enviar","cancelar"->"PLANEJADO";case "iniciar"->"ENVIADO_CORTE";case "finalizar"->"EM_CORTE";default->throw new AuthException(400,"Ação inválida.");};
        if(!lote.status().equals(esperado))throw new AuthException(409,"A situação deste lote mudou. Atualize a lista.");
        String proximo=switch(acao){case "enviar"->"ENVIADO_CORTE";case "cancelar"->"CANCELADO";case "iniciar"->"EM_CORTE";default->"CORTADO";};
        repo.status(id,proximo);
        base.auditar(Set.of("enviar","cancelar").contains(acao)?"PCP_LOTES":"CORTE_LOTES",proximo,"LOTE_CORTE",id,"Anterior: "+lote.status());
        return lote(id,false);
    }
    private void versao(long atual,long recebida){if(atual!=recebida)throw new AuthException(409,"Este registro foi alterado. Recarregue antes de salvar.");}
    private void validarPagina(int pagina,int tamanho,String busca){if(pagina<0||tamanho<1||tamanho>100||busca==null||busca.length()>150||(long)pagina*tamanho>Integer.MAX_VALUE)throw new AuthException(400,"Filtros ou paginação inválidos.");}
}
