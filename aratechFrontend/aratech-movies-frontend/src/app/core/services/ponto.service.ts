import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export interface DiaPonto { data: string; horarios: string[]; versao: number; ajustado: boolean; situacao?: string; aviso?: string; totalMinutos?: number; }
export interface EscalaPonto { diaSemana: number; horarios: string[]; extraMinutos: number; }
export interface HistoricoPonto { id: number; anteriores: string; novos: string; motivo: string; autor: string; alteradoEm: string; }
@Injectable({ providedIn: 'root' })
export class PontoService {
  constructor(private http: HttpClient) {}
  relogio() { return this.http.get<{instante:string;data:string}>('/aratech/auth/meu-ponto/relogio'); }
  marcar(dados: {data:string;versao:number}) { return this.http.post<DiaPonto>('/aratech/auth/meu-ponto/marcacoes',dados); }
  escala(id:number) { return this.http.get<EscalaPonto[]>(`/aratech/rh/ponto/${id}/escala`); }
  salvarEscala(id:number,dias:EscalaPonto[]) { return this.http.put<void>(`/aratech/rh/ponto/${id}/escala`,dias); }
  funcionarios() { return this.http.get<{id:number;nome:string;cargo:string;setor:string}[]>('/aratech/rh/ponto/funcionarios'); }
  listar(id: number, mes: string, proprio = false) { return this.http.get<DiaPonto[]>(proprio ? '/aratech/auth/meu-ponto' : `/aratech/rh/ponto/${id}`, { params: { mes } }); }
  funcionario(id: number) { return this.http.get<{ id: number; nome: string; cargo: string; setor: string }>(`/aratech/rh/ponto/${id}/funcionario`); }
  salvar(id: number, dia: DiaPonto, motivo: string) { return this.http.put<DiaPonto>(`/aratech/rh/ponto/${id}/${dia.data}`, { horarios: dia.horarios, versao: dia.versao, motivo }); }
  historico(id: number, data: string, proprio = false) { return this.http.get<HistoricoPonto[]>(proprio ? `/aratech/auth/meu-ponto/${data}/historico` : `/aratech/rh/ponto/${id}/${data}/historico`); }
}
