import { SolicitacaoPecaForm } from './solicitacao-peca.model';
import { normalizarChamado, validarChamado } from './chamado-form';
export function validarSolicitacaoPeca(form: SolicitacaoPecaForm): string {
  if (form.nomePeca.length > 150 || form.nomePeca.trim().length < 2 || /[\p{Cc}\p{Cf}]/u.test(form.nomePeca)) return 'Informe o nome da peca (2 a 150 caracteres).';
  if (form.codigo.length > 100 || /[\p{Cc}\p{Cf}]/u.test(form.codigo)) return 'Codigo deve ter ate 100 caracteres validos.';
  const q = form.quantidade;
  if (q === null || !Number.isFinite(q) || q <= 0 || q > 999999999999.999 || !/^[0-9]+(?:\.[0-9]{1,3})?$/.test(String(q))) return 'Informe uma quantidade positiva com ate 3 casas decimais.';
  if (!['un', 'kg', 'L', 'm', 'cx', 'par'].includes(form.unidade)) return 'Selecione a unidade.';
  if (!['CORRETIVA', 'PREVENTIVA', 'MELHORIA', 'ESTOQUE'].includes(form.finalidade)) return 'Selecione a finalidade.';
  return validarChamado({ ...form, tipo: 'CORRETIVA', descricao: form.observacoes });
}
export function normalizarSolicitacaoPeca(form: SolicitacaoPecaForm): SolicitacaoPecaForm {
  const comum = normalizarChamado({ ...form, tipo: 'CORRETIVA', descricao: form.observacoes });
  return { ...form, nomePeca: form.nomePeca.trim().replace(/ +/g, ' '), codigo: form.codigo.trim().replace(/ +/g, ' '),
    equipamento: comum.equipamento, solicitante: comum.solicitante, setor: comum.setor, telefone: comum.telefone, observacoes: comum.descricao };
}
