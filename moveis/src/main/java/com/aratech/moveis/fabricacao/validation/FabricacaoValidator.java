package com.aratech.moveis.fabricacao.validation;

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

public final class FabricacaoValidator {
    private FabricacaoValidator() {}
    public static void validarFicha(FichaGuardaRoupa ficha) {
        if(ficha.itens()==null||ficha.itens().isEmpty()||ficha.itens().size()>200)
            throw new AuthException(400,"Cadastre de 1 a 200 componentes.");
        Set<UUID> chaves=new HashSet<>();Set<String> codigos=new HashSet<>();
        for(var item:ficha.itens()) {
            if(item==null||item.chave()==null||!chaves.add(item.chave())||item.codigo()==null||!codigos.add(item.codigo().strip().toLowerCase(Locale.ROOT)))
                throw new AuthException(400,"Cada item deve ter chave e código únicos dentro da ficha.");
            if(item.cortar()&&(item.tipo()!=ItemGuardaRoupa.Tipo.PAINEL||item.unidade()!=ItemGuardaRoupa.Unidade.UN||item.quantidade()==null||item.quantidade().stripTrailingZeros().scale()>0||!positiva(item.comprimentoMm())||!positiva(item.larguraMm())||!positiva(item.espessuraMm())))
                throw new AuthException(400,"Itens de corte devem ser painéis em unidades inteiras, com comprimento, largura e espessura positivos.");
            if(item.bordas()!=null&&!item.bordas().isEmpty()&&(item.fitaBorda()==null||item.fitaBorda().isBlank()))
                throw new AuthException(400,"Informe a fita de borda dos itens com bordas selecionadas.");
        }
    }
    private static boolean positiva(BigDecimal v){return v!=null&&v.signum()>0;}
    public static FabricacaoRepository.FotoConteudo prepararFoto(MultipartFile arquivo) {
        if(arquivo==null||arquivo.isEmpty()||arquivo.getSize()>5*1024*1024)
            throw new AuthException(400,"Escolha uma imagem JPEG ou PNG de até 5 MB.");
        try(var entrada=arquivo.getInputStream()) {
            byte[] dados=entrada.readNBytes(5*1024*1024+1);
            if(dados.length>5*1024*1024)throw new AuthException(400,"A imagem ultrapassa 5 MB.");
            try(var imagemEntrada=ImageIO.createImageInputStream(new ByteArrayInputStream(dados))) {
                var leitores=ImageIO.getImageReaders(imagemEntrada);
                if(!leitores.hasNext())throw new AuthException(400,"Imagem inválida; use JPEG ou PNG.");
                var leitor=leitores.next();
                try {
                    leitor.setInput(imagemEntrada);
                    String formato=leitor.getFormatName().toLowerCase(Locale.ROOT);
                    if(!Set.of("jpeg","jpg","png").contains(formato))throw new AuthException(400,"Somente JPEG e PNG são aceitos.");
                    int largura=leitor.getWidth(0),altura=leitor.getHeight(0);
                    if(largura<1||altura<1||largura>6000||altura>6000||(long)largura*altura>16000000)
                        throw new AuthException(400,"Use uma imagem de até 6000 pixels por lado e 16 megapixels.");
                    var pixels=leitor.read(0);var saida=new ByteArrayOutputStream();
                    String tipo=formato.equals("png")?"png":"jpeg";
                    if(!ImageIO.write(pixels,tipo,saida)||saida.size()>5*1024*1024)
                        throw new AuthException(400,"A imagem reprocessada ultrapassa 5 MB.");
                    return new FabricacaoRepository.FotoConteudo("image/"+tipo,saida.toByteArray());
                } finally {leitor.dispose();}
            }
        } catch(IOException|IllegalArgumentException e) {throw new AuthException(400,"Não foi possível ler a imagem. Use JPEG ou PNG.");}
    }
}