/** Domínio interno, sem exigir extensão .com ou caixa postal externa. */
export function loginCorporativoValido(valor: string): boolean {
  const login = valor.trim();
  return login.length <= 150 && login.indexOf('@') <= 64 &&
    /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@aratechmoveis$/i.test(login);
}
