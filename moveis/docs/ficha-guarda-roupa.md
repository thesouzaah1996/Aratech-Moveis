# Guarda-roupas, lotes do PCP e Corte

Administração → **Guarda-roupas** mantém o catálogo. PCP → **Lotes** planeja o corte e mantém acesso à consulta **Produção concluída**, antes localizada em Administração. **Corte** substitui Comercial na navegação principal. Encomendas comerciais existentes permanecem nas APIs legadas, sem conversão em tarefas de corte.

## Ficha e componentes

O modelo tem código único, nome, descrição, medidas externas em mm e situação ativo/inativo. Quantidades são **por unidade de guarda-roupa**. Cada componente tem código próprio, grupo de montagem, tipo, material/referência do fornecedor, acabamento e unidade. São aceitos de 1 a 200 componentes, incluindo itens personalizados.

Sugestões da interface:

- Estrutura: laterais, base, tampo, divisórias, travessas, fundo e rodapé.
- Interior: prateleiras, portas, frente, contra-frente, laterais e fundo de gavetas.
- Ferragens: dobradiças e calços, corrediças, puxadores, parafusos, cavilhas, minifix, suportes, cabideiros, trilhos, roldanas, amortecedores, pés niveladores, cantoneiras, tapa-furos e kit de fixação à parede/antitombamento.
- Complementos: espelho, perfis, iluminação, organizadores, passa-fio, proteção e embalagem.

As sugestões não obrigam usar todos os itens. Cada componente aceita até 20 atributos nome/valor, para especificações como referência da ferragem, carga, fornecedor, dimensões da chapa, folgas e montagem.

## Medidas de fabricação e pesquisa

Marque **Incluir na lista de corte** apenas para painéis em UN com quantidade inteira. Informe comprimento, largura e espessura **finais de corte**, em mm. A medida externa do móvel não determina sozinha as peças: espessuras, encaixes, sobreposições e ferragens dependem do projeto.

Informe sentido do veio, bordas C1/C2 (lados do comprimento) e L1/L2 (lados da largura), referência da fita, furações, rasgos, usinagens e montagem. Painéis aceitam dimensões até 10000 mm e espessura até 200 mm. Demais itens podem ter medidas opcionais e quantidades fracionadas de até três casas decimais.

A pesquisa que orientou esses campos usa fabricantes:

- Duratex diferencia aplicações de espessuras para fundos e prateleiras, justificando registrar a espessura por peça, sem impor uma medida universal. [Espessuras de MDF — Duratex](https://www.duratex.com.br/blog/conheca-as-espessuras-de-mdf-existentes-e-saiba-como-usa-las).
- Corrediças têm comprimentos, cargas e folgas de montagem próprios. O projetista deve informar a ferragem e seguir sua ficha. [MOVENTO — Blum](https://www.blum.com/br/pt/products/runnersystems/movento/programme/), [Planejamento e furações — Blum](https://publications.blum.com/2022/catalogue/pt/419/).
- Portas de correr variam em montagem, peso e dimensões; trilhos e componentes devem corresponder ao sistema escolhido. [Sistemas de portas — Hettich](https://shop.hettich.com/gb_EN/Sliding-door-systems/Sliding-door-systems-for-inset-door-position/c/group5748370205184).
- As instruções de montagem de guarda-roupas também podem exigir conjuntos de fixação à parede. O cadastro inclui esse item para especificar o kit compatível com cada projeto. [Manual PAX — IKEA](https://static.islas.ikea.es/assets/pdfs/assembly/en/037/AA-2462037-2_pub.pdf).

As sugestões organizam dados para o cadastro; não são desenho técnico aprovado. Não há medidas de corte pré-fabricadas, otimização de chapas, cálculo de perdas de serra ou geração de programa CNC.

## Fotos e revisões

Salve a ficha primeiro. Adicione até quatro fotos à capa e a cada componente, com prévia, legenda e ampliação. JPEG/PNG até 5 MB, até 6000 pixels por lado e 16 megapixels. O servidor identifica o formato real e recodifica os pixels, removendo metadados. Fotos ficam no PostgreSQL, integradas ao backup e acessíveis apenas por endpoints autenticados.

Retirar foto remove a referência da ficha atual, preservando o conteúdo para lotes que já a utilizam. Alterações de ficha ou fotos incrementam a revisão. Versões antigas retornam 409.

## Do PCP ao Corte

1. PCP escolhe modelo ativo, de 1 a 10000 guarda-roupas e previsão a partir de hoje.
2. O lote nasce **Planejado**, com cópia imutável da ficha, revisão e referências de fotos. Exige pelo menos um painel com medidas de corte.
3. **Enviar ao Corte** disponibiliza o lote na fila. Planejamentos não enviados podem ser cancelados.
4. Corte inicia e conclui o corte integral: `PLANEJADO → ENVIADO_CORTE → EM_CORTE → CORTADO`. Cancelamento: `PLANEJADO → CANCELADO`.

Quantidades de componentes são multiplicadas pelas unidades do lote. Duas laterais por móvel em dez móveis resultam em vinte laterais. A área é a soma geométrica em m², sem aproveitamento ou perdas. Há exportação CSV e impressão da ficha.

O corte não movimenta estoque nem conclui outras etapas. Lotes históricos `OP-{id}` continuam em **Produção concluída** e na expedição existente.

## Acessos

- `ADMINISTRACAO_GUARDA_ROUPAS`: CONSULTAR visualiza; CADASTRAR cria; ALTERAR edita, inativa e gerencia fotos.
- `PCP_LOTES`: CONSULTAR visualiza lotes, modelos e produção concluída; CADASTRAR planeja; ALTERAR envia/cancela.
- `CORTE_LOTES`: CONSULTAR abre fila, fichas e fotos; ALTERAR inicia; FINALIZAR conclui.

Permissões amplas continuam funcionando. A migração preserva a consulta dos lotes movidos, sem conceder novas escritas. Corte usa permissões próprias; não herda permissões comerciais. A tarefa oficial Flyway registra os códigos de permissão antes de migrações que referenciam esses códigos.
