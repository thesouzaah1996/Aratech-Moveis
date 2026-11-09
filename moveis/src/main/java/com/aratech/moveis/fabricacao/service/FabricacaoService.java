package com.aratech.moveis.fabricacao.service;

import com.aratech.moveis.fabricacao.dto.*;
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

public interface FabricacaoService {
    Pagina<ModeloResumo> modelos(int pagina,int tamanho,String busca,boolean ativos);
    ModeloGuardaRoupa modelo(long id);
    ModeloGuardaRoupa salvar(Long id,FichaGuardaRoupa ficha);
    ModeloGuardaRoupa enviarFoto(long id,UUID item,long versao,String legenda,MultipartFile arquivo);
    ModeloGuardaRoupa retirarFoto(long id,long foto,long versao);
    FabricacaoRepository.FotoConteudo foto(long modelo,long foto);
    Pagina<LoteResumo> lotes(int pagina,int tamanho,String busca,boolean corte,String status);
    LoteCorte lote(long id,boolean corte);
    LoteCorte criarLote(NovoLoteCorte dto);
    LoteCorte transicao(long id,long versao,String acao);
    static void validarFicha(FichaGuardaRoupa ficha) { com.aratech.moveis.fabricacao.validation.FabricacaoValidator.validarFicha(ficha); }
    static FabricacaoRepository.FotoConteudo prepararFoto(MultipartFile arquivo) { return com.aratech.moveis.fabricacao.validation.FabricacaoValidator.prepararFoto(arquivo); }
}