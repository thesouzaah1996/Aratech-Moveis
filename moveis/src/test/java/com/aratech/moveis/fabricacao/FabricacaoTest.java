package com.aratech.moveis.fabricacao;

import com.aratech.moveis.fabricacao.dto.*;
import com.aratech.moveis.fabricacao.service.FabricacaoService;
import com.aratech.moveis.security.authorization.Permissoes;
import com.aratech.moveis.security.exception.AuthException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.math.BigDecimal;
import java.util.*;
import java.io.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

class FabricacaoTest {
    public static ItemGuardaRoupa painel(UUID chave,String codigo,BigDecimal comprimento) {
        return new ItemGuardaRoupa(chave,codigo,"Lateral",ItemGuardaRoupa.Tipo.PAINEL,"Estrutura","MDF","Branco",ItemGuardaRoupa.Unidade.UN,new BigDecimal("2"),true,comprimento,new BigDecimal("600"),new BigDecimal("18"),ItemGuardaRoupa.Veio.LONGITUDINAL,Set.of(ItemGuardaRoupa.Borda.C1),"Branco 1 mm","Furos conforme desenho","Lado esquerdo/direito",Map.of("Desenho","D-001"));
    }
    public static FichaGuardaRoupa ficha(String codigo,String nome,long versao,ItemGuardaRoupa...itens) {
        return new FichaGuardaRoupa(codigo,nome,"Modelo de teste",new BigDecimal("1800"),new BigDecimal("2200"),new BigDecimal("600"),true,versao,List.of(itens));
    }
    @Test void exigeMedidasDeCorteAntesDePlanejar() {
        var item=painel(UUID.randomUUID(),"LAT",null);
        assertEquals(400,assertThrows(AuthException.class,()->FabricacaoService.validarFicha(ficha("G1","Modelo",0,item))).status());
    }
    @Test void naoAceitaCodigosDuplicadosNemChavesReutilizadas() {
        UUID chave=UUID.randomUUID();var a=painel(chave,"LAT",BigDecimal.TEN);
        var b=painel(UUID.randomUUID(),"lat",BigDecimal.TEN);
        assertThrows(AuthException.class,()->FabricacaoService.validarFicha(ficha("G1","Modelo",0,a,b)));
        var c=painel(chave,"BASE",BigDecimal.TEN);
        assertThrows(AuthException.class,()->FabricacaoService.validarFicha(ficha("G1","Modelo",0,a,c)));
    }
    @Test void validaPixelsReaisSemConfiarNoMimeOuNomeDoArquivo() throws Exception {
        assertThrows(AuthException.class,()->FabricacaoService.prepararFoto(new MockMultipartFile("arquivo","foto.png","image/png","não é uma imagem".getBytes())));
        var bytes=new ByteArrayOutputStream();ImageIO.write(new BufferedImage(3,2,BufferedImage.TYPE_INT_RGB),"png",bytes);
        var resposta=FabricacaoService.prepararFoto(new MockMultipartFile("arquivo","qualquer.bin","application/octet-stream",bytes.toByteArray()));
        assertEquals("image/png",resposta.mime());assertNotNull(ImageIO.read(new ByteArrayInputStream(resposta.conteudo())));
    }
    @Test void permissoesSeparamCatalogoPlanejamentoECorte() {
        assertEquals("PCP_LOTES.CONSULTAR",Permissoes.necessaria("/aratech/pcp/lotes","GET"));
        assertEquals("PCP_LOTES.ALTERAR",Permissoes.necessaria("/aratech/pcp/lotes/corte/1/enviar","POST"));
        assertEquals("CORTE_LOTES.FINALIZAR",Permissoes.necessaria("/aratech/corte/lotes/1/finalizar","POST"));
        assertEquals("ADMINISTRACAO_GUARDA_ROUPAS.ALTERAR",Permissoes.necessaria("/aratech/administracao/guarda-roupas/1/fotos","POST"));
        assertEquals("CORTE_LOTES.CONSULTAR",Permissoes.necessaria("/aratech/corte/lotes/modelos/1/fotos/2","GET"));
    }
}
