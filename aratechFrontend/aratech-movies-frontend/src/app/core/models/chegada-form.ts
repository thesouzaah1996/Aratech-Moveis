import { RegistroChegadaForm } from './registro-chegada.model';
export function validarChegada(form: RegistroChegadaForm): string {
  const texto = (valor: string, min: number, max: number) => valor.length <= max && valor.trim().length >= min && !/[\p{Cc}\p{Cf}]/u.test(valor);
  if (!notaFiscalValida(form.notaFiscal.trim())) return 'Informe o numero da nota fiscal (1 a 9 digitos, diferente de zero) ou uma chave de acesso de 44 digitos com digito verificador valido.';
  if (!texto(form.empresa, 2, 150)) return 'Informe a empresa (2 a 150 caracteres).';
  if (!texto(form.nomeMotorista, 3, 150)) return 'Informe o motorista (3 a 150 caracteres).';
  if (!/^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$/.test(form.placa.trim().toUpperCase())) return 'Informe uma placa brasileira valida, tradicional ou Mercosul.';
  if (!texto(form.descricaoCarga, 3, 1000)) return 'Descreva a carga (3 a 1000 caracteres).';
  if (!['ALMOXARIFADO', 'CARREGAMENTO'].includes(form.setorResponsavel)) return 'Selecione almoxarifado ou carregamento.';
  if (form.setorResponsavel === 'CARREGAMENTO' && !['CARREGAR', 'DESCARREGAR'].includes(form.tipoOperacao ?? '')) return 'Selecione carregar ou descarregar.';
  return '';
}
export function normalizarChegada(form: RegistroChegadaForm): RegistroChegadaForm {
  return { ...form, tipoOperacao: form.setorResponsavel === 'CARREGAMENTO' ? form.tipoOperacao : null, notaFiscal: form.notaFiscal.trim(), empresa: form.empresa.trim().replace(/ +/g, ' '),
    nomeMotorista: form.nomeMotorista.trim().replace(/ +/g, ' '), placa: form.placa.trim().toUpperCase().replace('-', ''),
    descricaoCarga: form.descricaoCarga.trim().replace(/ +/g, ' ') };
}

function notaFiscalValida(nf: string): boolean {
  if (/^[0-9]{1,9}$/.test(nf)) return !/^0+$/.test(nf);
  if (!/^[0-9]{44}$/.test(nf) || /^([0-9])\1{43}$/.test(nf)) return false;
  let soma = 0, peso = 2;
  for (let i = 42; i >= 0; i--) {
    soma += Number(nf[i]) * peso;
    peso = peso === 9 ? 2 : peso + 1;
  }
  const digito = 11 - soma % 11;
  return Number(nf[43]) === (digito >= 10 ? 0 : digito);
}
