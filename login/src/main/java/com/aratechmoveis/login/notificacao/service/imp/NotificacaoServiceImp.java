package com.aratechmoveis.login.notificacao.service.imp;

import com.aratechmoveis.login.funcionario.entity.Funcionario;
import com.aratechmoveis.login.funcionario.repository.FuncionarioRepository;
import com.aratechmoveis.login.notificacao.dto.NotificacaoDTO;
import com.aratechmoveis.login.notificacao.entity.Notificacao;
import com.aratechmoveis.login.notificacao.enums.TipoNotificacao;
import com.aratechmoveis.login.notificacao.repo.NotificacaoRepo;
import com.aratechmoveis.login.notificacao.service.NotificacaoService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificacaoServiceImp implements NotificacaoService {

    private static final String LOGO_CID = "logoAratech";
    private static final String LOGO_PATH_PADRAO = "static/img/aratech-logo-dashboard.png";

    private final NotificacaoRepo notificacaoRepo;
    private final FuncionarioRepository funcionarioRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String remetente;

    @Override
    @Async
    public void enviarEmail(NotificacaoDTO notificacaoDTO, Funcionario funcionario) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(remetente);
            helper.setTo(notificacaoDTO.getDestinatario());
            helper.setSubject(notificacaoDTO.getAssunto());

            if (notificacaoDTO.getNomeTemplate() != null) {
                Context context = new Context();
                context.setVariables(notificacaoDTO.getVariaveisTemplate());
                String conteudoHtml = templateEngine.process(notificacaoDTO.getNomeTemplate(), context);

                helper.setText(conteudoHtml, true);
                String logoPath = notificacaoDTO.getLogoPath() != null ? notificacaoDTO.getLogoPath() : LOGO_PATH_PADRAO;
                helper.addInline(LOGO_CID, new ClassPathResource(logoPath));
            } else {
                helper.setText(notificacaoDTO.getMensagem(), true);
            }

            mailSender.send(mimeMessage);
            log.info("Email enviado com sucesso.");

            Notificacao notificacaoParaSalvar = Notificacao.builder()
                    .destinatario(notificacaoDTO.getDestinatario())
                    .assunto(notificacaoDTO.getAssunto())
                    .mensagem(notificacaoDTO.getMensagem())
                    .tipoNotificacao(TipoNotificacao.EMAIL)
                    .funcionario(funcionarioRepository.getReferenceById(funcionario.getId()))
                    .build();

            notificacaoRepo.save(notificacaoParaSalvar);

        } catch (Exception e) {
            log.error("Erro ao enviar email para {}", notificacaoDTO.getDestinatario(), e);
        }
    }
}
