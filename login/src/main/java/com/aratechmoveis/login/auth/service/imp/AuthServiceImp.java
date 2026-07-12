package com.aratechmoveis.login.auth.service.imp;

import com.aratechmoveis.login.auth.entity.CodigoResetSenha;
import com.aratechmoveis.login.auth.repository.CodigoResetSenhaRepo;
import com.aratechmoveis.login.auth.request.LoginRequest;
import com.aratechmoveis.login.auth.request.PrimeiroAcessoRequest;
import com.aratechmoveis.login.auth.service.AuthService;
import com.aratechmoveis.login.auth.service.GeradorCodigo;
import com.aratechmoveis.login.exceptions.BadRequestException;
import com.aratechmoveis.login.exceptions.NotFoundException;
import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.entity.Perfil;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.funcionario.repository.PerfilRepository;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;
import com.aratechmoveis.login.notificacao.service.NotificacaoService;
import com.aratechmoveis.login.response.LoginResponse;
import com.aratechmoveis.login.response.Response;
import com.aratechmoveis.login.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {

    private final FuncionarioRepository funcionarioRepository;
    private final PerfilRepository perfilRepository;
    private final NotificacaoService notificacaoService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
//
    private final GeradorCodigo geradorCodigo;
    private final CodigoResetSenhaRepo codigoResetSenhaRepo;

    @Value("${password.reset.link}")
    private String resetLink;

    @Value("${password.primeiro.acesso.link}")
    private String primeiroAcessoLink;

    @Override
    public Response<LoginResponse> login(LoginRequest loginRequest) {
        String email = loginRequest.getEmailCorporativo();
        String senha = loginRequest.getSenha();

        Funcionario funcionario = funcionarioRepository.findByEmailCorporativo(email)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado"));

        if (!passwordEncoder.matches(senha, funcionario.getSenha())) {
            throw new BadRequestException("Senha ou email não batem");
        }

        List<String> perfis = funcionario.getPerfis().stream().map(Perfil::getNome).toList();
        String token = jwtService.gerarToken(funcionario.getEmailCorporativo(), perfis);

        LoginResponse loginResponse = LoginResponse.builder()
                .perfis(perfis)
                .token(token)
                .build();

        return Response.<LoginResponse>builder()
                .status(200)
                .mensagem("Login realizado com sucesso")
                .dados(loginResponse)
                .build();
    }

    @Override
    @Transactional
    public Response<?> solicitarRedefinicaoSenha(String email) {
        Funcionario funcionario = funcionarioRepository.findByEmailCorporativo(email)
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado. Para alterar a senha, confira se o email está correto."));

        codigoResetSenhaRepo.deleteByFuncionarioIdFuncionario(funcionario.getIdFuncionario());

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
    @Transactional
    public Response<?> solicitarPrimeiroAcesso(String emailPessoal) {
        Funcionario funcionario = funcionarioRepository.findByEmailPessoal(emailPessoal)
                .orElseThrow(() -> new NotFoundException("O email deve ser o mesmo informado na admissão do funcionário."));

        codigoResetSenhaRepo.deleteByFuncionarioIdFuncionario(funcionario.getIdFuncionario());

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
                .logoPath("static/img/aratech-logo-dashboard.png")
                .build();

        log.info("Enviando email para primeiro acesso");
        notificacaoService.enviarEmail(notificacaoPrimeiroAcesso, funcionario);

        return Response.builder()
                .status(200)
                .mensagem("Codigo para primeiro acesso enviado com sucesso")
                .build();
    }

    @Override
    @Transactional
    public Response<?> confirmarPrimeiroAcesso(PrimeiroAcessoRequest primeiroAcessoRequest) {
        Funcionario funcionario = funcionarioRepository.findByIdFuncionario(primeiroAcessoRequest.getIdFuncionario())
                .orElseThrow(() -> new NotFoundException("Funcionário não encontrado. Para concluir o primeiro acesso, confira se o id está correto."));

        CodigoResetSenha codigoResetSenha = codigoResetSenhaRepo.findByCodigo(primeiroAcessoRequest.getCodigo())
                .filter(codigo -> codigo.getFuncionario().getIdFuncionario().equals(funcionario.getIdFuncionario()))
                .orElseThrow(() -> new NotFoundException("Código inválido."));

        if (codigoResetSenha.isUsado()) {
            throw new NotFoundException("Código já utilizado.");
        }

        if (codigoResetSenha.getDataExpiracao().isBefore(LocalDateTime.now())) {
            throw new NotFoundException("Código expirado.");
        }

        funcionario.setSenha(passwordEncoder.encode(primeiroAcessoRequest.getNovaSenha()));
        funcionario.setPrimeiroLogin(false);
        funcionarioRepository.save(funcionario);

        codigoResetSenha.setUsado(true);
        codigoResetSenhaRepo.save(codigoResetSenha);

        return Response.builder()
                .status(200)
                .mensagem("Sucesso")
                .build();
    }


    private LocalDateTime calcularDataExpiracao() {
        return LocalDateTime.now().plusHours(5);
    }
}
