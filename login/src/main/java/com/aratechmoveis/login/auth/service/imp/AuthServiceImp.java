package com.aratechmoveis.login.auth.service.imp;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import com.aratechmoveis.login.auth.repository.CodigoResetSenhaRepo;
import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.AuthService;
import com.aratechmoveis.login.auth.service.GeradorCodigo;
import com.aratechmoveis.login.exceptions.NotFoundException;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.funcionario.repository.PerfilRepository;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;
import com.aratechmoveis.login.notificacao.service.NotificacaoService;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final FuncionarioRepository funcionarioRepository;
    private final PerfilRepository perfilRepository;
    private final NotificacaoService notificacaoService;
    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//
    private final GeradorCodigo geradorCodigo;
    private final CodigoResetSenhaRepo codigoResetSenhaRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Value("${password.primeiro.acesso.link}")
    private String primeiroAcessoLink;

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        return null;
    }

    @Override
    public Response<?> solicitarRedefinicaoSenha(String email) {
        Funcionario funcionario = funcionarioRepository.findByEmailCorporativo(email)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado. Para alterar a senha, confira se o email está correto."));

        codigoResetSenhaRepo.deleteByUserId(funcionario.getIdFuncionario());

        String codigo = geradorCodigo.gerarCodigoUnico();

        CodigoResetSenha codigoReset = CodigoResetSenha.builder()
                .funcionario(funcionario)
                .codigo(codigo)
                .dataExpiracao(calcularDataExpiracao())
                .usado(false)
                .build();

        codigoResetSenhaRepo.save(codigoReset);

        NotificacaoDTO resetSenhaEmail = NotificacaoDTO.builder()
                .destinatario(funcionario.getEmailPessoal())
                .assunto("Codigo para reset de senha")
                .nomeTemplate("reset-senha")
                .variaveisTemplate(Map.of(
                        "nome", funcionario.getNomeFuncionario(),
                        "resetLink", resetLink + codigo
                ))
                .build();

        notificacaoService.enviarEmail(resetSenhaEmail, funcionario);

        return Response.builder()
                .status(200)
                .mensagem("Codigo para alterar senha enviado com sucesso")
                .build();
    }

    @Override
    public Response<?> definirSenhaPrimeiroAcesso(PrimeiroAcessoRequest primeiroAcessoRequest) {
        Funcionario funcionario = funcionarioRepository.findByEmailPessoal(primeiroAcessoRequest.getEmailPessoal())
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado. Para alterar a senha, confira se o email pessoal está correto."));

        String codigo = geradorCodigo.gerarCodigoUnico();

        CodigoResetSenha codigoResetSenha = CodigoResetSenha.builder()
                .funcionario(funcionario)
                .codigo(codigo)
                .dataExpiracao(calcularDataExpiracao())
                .usado(false)
                .build();

        codigoResetSenhaRepo.save(codigoResetSenha);

        NotificacaoDTO notificacaoPrimeiroAcesso = NotificacaoDTO.builder()
                .destinatario(funcionario.getEmailPessoal())
                .assunto("Codigo para primeiro acesso ao sistema")
                .nomeTemplate("primeiro-acesso")
                .variaveisTemplate(Map.of(
                        "nome", funcionario.getNomeFuncionario(),
                        "linkPrimeiroAcesso", primeiroAcessoLink + codigo
                ))
                .build();

        log.info("Enviando email para primeiro acesso");
        notificacaoService.enviarEmail(notificacaoPrimeiroAcesso, funcionario);

        funcionario.setSenha(passwordEncoder.encode(primeiroAcessoRequest.getNovaSenha()));

        if (funcionario.isPrimeiroLogin()) {
            funcionario.setPrimeiroLogin(false);
        }

        funcionarioRepository.save(funcionario);

        return Response.builder()
                .status(200)
                .mensagem("Sucesso")
                .build();
    }


    private LocalDateTime calcularDataExpiracao() {
        return LocalDateTime.now().plusHours(5);
    }
}
