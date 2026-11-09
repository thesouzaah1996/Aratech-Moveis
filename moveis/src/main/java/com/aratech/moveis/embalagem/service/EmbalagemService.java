package com.aratech.moveis.embalagem.service;

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

public interface EmbalagemService {
    List<Esteira> opcoesEsteiras();
    List<PainelEsteira> painel(String busca,boolean ativas);
    Esteira salvarEsteira(Long id,EsteiraEntrada dto);
    Pagina<LoteResumo> origens(int pagina,int tamanho,String busca);
    Pagina<ResumoEmbalagem> lotes(int pagina,int tamanho,String busca,String status);
    LoteEmbalagem lote(long id);
    LoteEmbalagem receber(NovoLoteEmbalagem dto);
    LoteEmbalagem cancelar(long id,ComandoEmbalagem dto);
    LoteEmbalagem operar(long esteiraId,long etapaId,String acao,ComandoEmbalagem dto);
}