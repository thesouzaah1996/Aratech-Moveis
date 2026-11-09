package com.aratech.moveis.pcp.lotes;

import static com.aratech.moveis.pcp.lotes.ProducaoDtos.*;
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

public interface LotesProducaoService {
    List<Setor> setores();
    List<OpcaoVariante> variantes(String busca);
    Variante variante(long id);
    List<Origem> origens(String busca);
    void exigirOrigemConcluida(long origem);
    void exigirOrigemSemLotes(long origem);
    void exigirPedidoSemLotes(long pedido);
    void validarReprogramacao(long origem,String produto,int quantidade);
    Variante salvar(Long id,FichaVariante d);
    Calculo calcular(Variante v,int quantidade);
    Lote criar(NovoLote d);
    Lote lote(long id);
    Lote revisar(long id,long revisao);
    Pagina<Resumo> listar(int pagina,String busca);
    Lote liberar(long id,Liberar d);
    Setor setor(String caminho,String acao);
    Pagina<Ordem> fila(Setor s,int pagina,String busca);
    Ordem ordem(Setor s,long id);
    Ordem acao(Setor s,long id,String acao,Acao d);
    Ordem consumir(Setor s,long id,Consumo d);
}