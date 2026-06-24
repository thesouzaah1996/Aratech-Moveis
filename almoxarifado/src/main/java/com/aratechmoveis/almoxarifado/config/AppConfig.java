package com.aratechmoveis.almoxarifado.config;

import com.aratechmoveis.almoxarifado.produto.dto.ProdutoDTO;
import com.aratechmoveis.almoxarifado.produto.modelo.Produto;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        mapper.createTypeMap(Produto.class, ProdutoDTO.class)
                .setPostConverter(ctx -> {
                    Produto src = ctx.getSource();
                    ProdutoDTO dst = ctx.getDestination();
                    if (src.getCategoria() != null) {
                        dst.setCategoriaID(src.getCategoria().getId());
                    }
                    if (src.getFornecedor() != null) {
                        dst.setFornecedorID(src.getFornecedor().getId());
                    }
                    return dst;
                });

        return mapper;
    }
}
