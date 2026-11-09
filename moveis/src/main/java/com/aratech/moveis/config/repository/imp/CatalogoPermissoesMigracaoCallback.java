package com.aratech.moveis.config.repository.imp;

import com.aratech.moveis.security.authorization.Permissoes;
import org.flywaydb.core.api.callback.*;
import java.sql.*;

/** Registra códigos antes de migrações que preservam vínculos de perfis existentes. */
public final class CatalogoPermissoesMigracaoCallback implements Callback {
    public boolean supports(Event evento,Context contexto){return evento==Event.BEFORE_EACH_MIGRATE;}
    public boolean canHandleInTransaction(Event evento,Context contexto){return true;}
    public String getCallbackName(){return "catalogo_de_permissoes_antes_da_migracao";}
    public void handle(Event evento,Context contexto){registrar(contexto);}
    public static void registrar(Context contexto) {
        String schema=contexto.getConfiguration().getDefaultSchema();
        if(schema==null||!schema.matches("[a-z_][a-z0-9_]{0,62}"))throw new IllegalArgumentException("Schema inválido.");
        try(var consulta=contexto.getConnection().prepareStatement("SELECT to_regclass(?)")) {
            consulta.setString(1,schema+".acesso_permissao");
            try(var r=consulta.executeQuery()){r.next();if(r.getString(1)==null)return;}
            try(var insert=contexto.getConnection().prepareStatement("INSERT INTO \""+schema+"\".acesso_permissao(codigo) VALUES (?) ON CONFLICT DO NOTHING")) {
                for(String codigo:Permissoes.catalogo()){insert.setString(1,codigo);insert.addBatch();}insert.executeBatch();
            }
        } catch(SQLException e){throw new IllegalStateException("Não foi possível registrar o catálogo de permissões da migração.",e);}
    }
}
