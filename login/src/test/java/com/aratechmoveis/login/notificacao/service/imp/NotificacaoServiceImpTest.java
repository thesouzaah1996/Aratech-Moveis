package com.aratechmoveis.login.notificacao.service.imp;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;
import com.aratechmoveis.login.notificacao.entity.Notificacao;
import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import com.aratechmoveis.login.notificacao.repo.NotificacaoRepo;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificacaoServiceImp")
class NotificacaoServiceImpTest {

    @Mock
    private NotificacaoRepo notificacaoRepo;

    @Mock
    private FuncionarioRepository funcionarioRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @InjectMocks
    private NotificacaoServiceImp notificacaoService;

    private Funcionario umFuncionario() {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(1L);
        funcionario.setIdFuncionario(10L);
        funcionario.setNomeFuncionario("Maria Silva");
        funcionario.setEmailPessoal("maria@gmail.com");
        funcionario.setEmailCorporativo("maria@aratech.com");
        return funcionario;
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(notificacaoService, "remetente", "no-reply@aratech.com");
        given(mailSender.createMimeMessage()).willReturn(new MimeMessage((Session) null));
    }

    @Nested
    @DisplayName("enviarEmail")
    class EnviarEmail {

        @Test
        @DisplayName("deve renderizar template, enviar email e salvar notificação com sucesso")
        void deveEnviarEmailComTemplateComSucesso() {
            Funcionario funcionario = umFuncionario();
            NotificacaoDTO dto = NotificacaoDTO.builder()
                    .destinatario(funcionario.getEmailPessoal())
                    .assunto("Codigo para reset de senha")
                    .nomeTemplate("reset-senha")
                    .variaveisTemplate(Map.of("nome", funcionario.getNomeFuncionario()))
                    .build();

            given(templateEngine.process(eq("reset-senha"), any())).willReturn("<html></html>");
            given(funcionarioRepository.getReferenceById(1L)).willReturn(funcionario);

            notificacaoService.enviarEmail(dto, funcionario);

            then(mailSender).should().send(any(MimeMessage.class));
            then(notificacaoRepo).should().save(argThat((Notificacao n) ->
                    n.getTipoNotificacao() == TipoNotificacao.EMAIL
                            && n.getDestinatario().equals(funcionario.getEmailPessoal())));
        }

        @Test
        @DisplayName("deve enviar email com mensagem simples quando não há template")
        void deveEnviarEmailSemTemplateComSucesso() {
            Funcionario funcionario = umFuncionario();
            NotificacaoDTO dto = NotificacaoDTO.builder()
                    .destinatario(funcionario.getEmailPessoal())
                    .assunto("Aviso")
                    .mensagem("Mensagem simples")
                    .build();

            given(funcionarioRepository.getReferenceById(1L)).willReturn(funcionario);

            notificacaoService.enviarEmail(dto, funcionario);

            then(mailSender).should().send(any(MimeMessage.class));
            then(notificacaoRepo).should().save(any(Notificacao.class));
            then(templateEngine).should(never()).process(any(String.class), any());
        }

        @Test
        @DisplayName("não deve propagar exceção nem salvar notificação quando o envio falha")
        void naoDevePropagarExcecaoQuandoEnvioFalha() {
            Funcionario funcionario = umFuncionario();
            NotificacaoDTO dto = NotificacaoDTO.builder()
                    .destinatario(funcionario.getEmailPessoal())
                    .assunto("Aviso")
                    .mensagem("Mensagem simples")
                    .build();

            willThrow(new RuntimeException("erro de envio")).given(mailSender).send(any(MimeMessage.class));

            notificacaoService.enviarEmail(dto, funcionario);

            then(notificacaoRepo).should(never()).save(any());
        }
    }
}
