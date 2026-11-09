import { ChamadoTiForm, TIPOS_CHAMADO_TI, MODULOS_CHAMADO_TI } from './chamado-ti.model';
export function validarChamadoTi(f: ChamadoTiForm): string {
  const texto = (s: string, min: number, max: number, multiline = false) => s.trim().length >= min && s.length <= max && !/[\p{Cc}\p{Cf}]/u.test(multiline ? s.replace(/[\r\n\t]/g,'') : s);
  if (!TIPOS_CHAMADO_TI.includes(f.tipo as any)) return 'Selecione o tipo de problema.';
  if (!['Baixa','Média','Alta','Crítica'].includes(f.prioridade)) return 'Selecione a prioridade.';
  if (!MODULOS_CHAMADO_TI.includes(f.moduloAfetado)) return 'Selecione um modulo valido ou deixe em branco.';
  if (!texto(f.titulo,3,150)) return 'Informe o titulo (3 a 150 caracteres).';
  if (!texto(f.solicitante,3,150)) return 'Informe o solicitante (3 a 150 caracteres).';
  if (!texto(f.setor,2,100)) return 'Informe o setor (2 a 100 caracteres).';
  const contato=f.contato.trim();
  if (contato && (!/^[0-9() +\-]{1,25}$/.test(contato) || !/^(?:[0-9]{2,6}|[1-9][0-9]{9,10})$/.test(contato.replace(/[^0-9]/g,'')))) return 'Informe um ramal de 2 a 6 digitos ou um telefone com DDD (10 ou 11 digitos).';
  if (!texto(f.descricao,3,3000,true)) return 'Descreva o problema (3 a 3000 caracteres).';
  if (!texto(f.tentativasSolucao,0,2000,true)) return 'Tentativas de solucao devem ter ate 2000 caracteres validos.';
  return '';
}
export function normalizarChamadoTi(f: ChamadoTiForm): ChamadoTiForm {
  const texto=(s:string)=>s.trim().replace(/ +/g,' ');
  return { ...f, titulo:texto(f.titulo), solicitante:texto(f.solicitante), setor:texto(f.setor), contato:f.contato.replace(/[^0-9]/g,''), descricao:f.descricao.trim(), tentativasSolucao:f.tentativasSolucao.trim() };
}
