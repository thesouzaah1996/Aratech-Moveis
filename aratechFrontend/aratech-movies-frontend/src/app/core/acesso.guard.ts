import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './services/auth.service';
import { TELAS } from './permissoes';

export function permissaoDaRota(path: string): string | null {
  if (path.startsWith('recursos-humanos/perfil')) return 'ACESSOS.GERENCIAR';
  const tela=Object.keys(TELAS).sort((a,b)=>b.length-a.length).find(t=>path===t||path.startsWith(t+'/'));
  if(tela)return TELAS[tela]+(['manutencao/solicitar-manutencao','manutencao/solicitar-peca','suporte/chamado-ti','administracao/receber-assistencia'].includes(tela)?'.CADASTRAR':'.CONSULTAR');
  const segmento = path.split('/')[0];
  const modulo: Record<string, string> = { 'recursos-humanos': 'RH', manutencao: 'MANUTENCAO', assistencia: 'ASSISTENCIA', administracao: 'ADMINISTRACAO', almoxarifado: 'ALMOXARIFADO', carregamento: 'CARREGAMENTO', portaria: 'PORTARIA', financeiro: 'FINANCEIRO', compras: 'COMPRAS', pcp: 'PCP', suporte: 'SUPORTE', comercial:'COMERCIAL', corte:'CORTE', embalagem:'EMBALAGEM' };
  return modulo[segmento] ? modulo[segmento] + '.CONSULTAR' : null;
}
export const acessoGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService), router = inject(Router);
  return auth.verificarSessao().pipe(map(usuario => {
    const path = route.routeConfig?.path ?? '';
    if (usuario.trocarSenha && path !== 'trocar-senha') return router.createUrlTree(['/trocar-senha']);
    const permissao = permissaoDaRota(path);
    if (!permissao || auth.temPermissao(permissao) || path === 'recursos-humanos' && auth.temPermissao('ACESSOS.GERENCIAR')) return true;
    return router.createUrlTree(['/sem-acesso']);
  }), catchError(erro => of(router.createUrlTree(erro.status === 401 ? ['/login'] : ['/falha-conexao'], erro.status === 401 ? { queryParams: { returnUrl: state.url } } : {}))));
};
