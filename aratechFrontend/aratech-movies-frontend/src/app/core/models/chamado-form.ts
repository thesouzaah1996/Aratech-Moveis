import { ChamadoForm } from './chamado.model';

export function validarChamado(form: ChamadoForm): string {
  const texto = (valor: string, min: number, max: number) => valor.length <= max && valor.trim().length >= min && !/[\p{Cc}\p{Cf}]/u.test(valor);
  if (!texto(form.equipamento, 2, 150)) return 'Informe o equipamento (2 a 150 caracteres).';
  if (!['CORRETIVA', 'PREVENTIVA'].includes(form.tipo)) return 'Selecione o tipo de manutencao.';
  if (!['BAIXA', 'MEDIA', 'ALTA'].includes(form.prioridade)) return 'Selecione a prioridade.';
  if (!texto(form.solicitante, 3, 150)) return 'Informe o solicitante (3 a 150 caracteres).';
  if (!texto(form.setor, 2, 100)) return 'Informe o setor (2 a 100 caracteres).';
  const telefone = form.telefone.trim();
  if (telefone && (!/^[0-9() +\-]{1,25}$/.test(telefone) || !/^[1-9][0-9]{9,10}$/.test(telefone.replace(/[^0-9]/g, '')))) return 'Informe um telefone com DDD e 10 ou 11 digitos, ou deixe em branco.';
  if (form.descricao.length > 3000 || form.descricao.trim().length < 3 || /[\p{Cc}\p{Cf}]/u.test(form.descricao.replace(/[\r\n\t]/g, ''))) return 'Descreva o problema (3 a 3000 caracteres).';
  return '';
}
export function normalizarChamado(form: ChamadoForm): ChamadoForm {
  const texto = (valor: string) => valor.trim().replace(/ +/g, ' ');
  return { ...form, equipamento: texto(form.equipamento), solicitante: texto(form.solicitante),
    setor: texto(form.setor), telefone: form.telefone.replace(/[^0-9]/g, ''), descricao: form.descricao.trim() };
}
