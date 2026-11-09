package com.aratech.moveis.shared.auditoria.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Inherited
@Target(ElementType.TYPE)
public @interface AuditadoNoServico {}
