# Guía de contribución

Gracias por contribuir al proyecto Biblioteca. Este documento define un flujo de trabajo simple y consistente para el laboratorio.

## Ramas

- `main`: versión estable y desplegable. No se modifica directamente durante la actividad.
- `dev`: rama de desarrollo donde se preparan los cambios antes del Pull Request.

## Flujo de trabajo

1. Actualiza tu repositorio local.
2. Cambia a la rama `dev`.
3. Realiza un cambio pequeño y comprobable. Para el laboratorio puede ser un cambio visual en `frontend/index.html` o un cambio de documentación Markdown.
4. Revisa los archivos modificados.
5. Crea un commit descriptivo.
6. Publica `dev` en GitHub.
7. Crea un Pull Request desde `dev` hacia `main`.
8. Revisa los checks de GitHub Actions.
9. Fusiona el Pull Request cuando las validaciones terminen correctamente.

Ejemplo:

```bash
git switch dev
git pull origin dev
git status
git add .
git commit -m "docs: actualiza documentación del proyecto"
git push origin dev
```

## Mensajes de commit

Usa mensajes breves que expliquen el propósito del cambio. Ejemplos:

```text
docs: agrega documentos Markdown
fix: corrige configuración del gateway
feat: agrega endpoint de búsqueda
ci: actualiza pipeline de despliegue
```

## Pull Requests

El Pull Request debe indicar:

- Qué se modificó.
- Por qué se realizó el cambio.
- Cómo se comprobó.

No se deben incluir credenciales, claves privadas, archivos `.pem`, contraseñas o secretos.

## Revisión

Antes del merge:

- El proyecto debe compilar.
- El workflow de validación debe finalizar correctamente.
- El diff del Pull Request debe corresponder al cambio solicitado.
