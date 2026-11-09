package com.aratech.moveis.security.repository;

import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.List;
import java.util.Map;

public interface PerfilUsuarioRepository {
    List<Map<String, Object>> consultarFuncionario(Long id);
}
