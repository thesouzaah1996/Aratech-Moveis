import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type StatusLoteAssistencia = 'Não iniciado' | 'Em andamento' | 'Finalizado';
export interface LoteAssistencia {
  id: number; origemId?: number; codigo: string; produto: string; empresa: string;
  quantidade: number; dataEntrada: string; responsavel: string; defeito: string;
  status: StatusLoteAssistencia; iniciadoEm?: string; finalizadoEm?: string; carregadoEm?: string;
}

@Injectable({ providedIn: 'root' })
export class LoteAssistenciaService {
  private readonly url = '/aratech/assistencia/lotes';
  constructor(private http: HttpClient) {}
  listar(): Observable<LoteAssistencia[]> { return this.http.get<LoteAssistencia[]>(this.url); }
  receber(lote: { id: number; codigo: string; produto: string; quantidade: number; responsavel: string }): Observable<LoteAssistencia> {
    return this.http.post<LoteAssistencia>(this.url, { ...lote, origemId: lote.id });
  }
  avancar(id: number, acao: 'iniciar' | 'finalizar' | 'carregar'): Observable<LoteAssistencia> {
    return this.http.post<LoteAssistencia>(`${this.url}/${id}/${acao}`, {});
  }
}
