export interface ModalDaTela {
  show(): void;
  hide(): void;
  dispose(): void;
}

// Mantem a instancia viva ate o Bootstrap terminar as animacoes de abertura e fechamento.
export function criarModal(elemento: HTMLElement): ModalDaTela {
  const instancia = (window as any).bootstrap.Modal.getOrCreateInstance(elemento);
  let aberto = false;
  let descartando = false;
  const aoMostrar = () => { aberto = true; };
  const aoOcultar = () => { aberto = false; };
  elemento.addEventListener('show.bs.modal', aoMostrar);
  elemento.addEventListener('hidden.bs.modal', aoOcultar);

  const fechar = () => {
    if (!aberto) return;
    const depoisDeAbrir = () => instancia.hide();
    const limpar = () => {
      elemento.removeEventListener('shown.bs.modal', depoisDeAbrir);
      elemento.removeEventListener('hidden.bs.modal', limpar);
    };
    elemento.addEventListener('shown.bs.modal', depoisDeAbrir, { once: true });
    elemento.addEventListener('hidden.bs.modal', limpar, { once: true });
    instancia.hide();
  };

  return {
    show: () => { if (!descartando) instancia.show(); },
    hide: fechar,
    dispose: () => {
      if (descartando) return;
      descartando = true;
      const concluir = () => {
        elemento.removeEventListener('show.bs.modal', aoMostrar);
        elemento.removeEventListener('hidden.bs.modal', aoOcultar);
        instancia.dispose();
      };
      if (aberto) {
        elemento.addEventListener('hidden.bs.modal', () => { concluir(); elemento.remove(); }, { once: true });
        fechar();
      } else concluir();
    }
  };
}
