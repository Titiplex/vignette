# Documentation API (Java / Spring Boot)

Automatically generated API documentation with **springdoc-openapi** for backend, then **statically published on
GitHub Pages**.

## 1) Local documentation (auto-updated)

When the app is launched :

- Swagger UI : `http://localhost:8081/api/docs`
- OpenAPI JSON : `http://localhost:8081/api/docs/openapi`

```bash
mvn spring-boot:run
```

`springdoc-openapi` scans springs controllers at each launch, so the doc is updated automatically when Java endpoints
change.

## 2) Publishing on GitHub Pages

A GitHub Actions workflow has been added :

- File : `.github/workflows/publish-api-docs.yml`
- Trigger :
    - manual (`workflow_dispatch`)
    - pushes on `main`

Ce workflow :

1. build + launches API,
2. exports `http://127.0.0.1:8081/api/docs/openapi` towards `docs/openapi.json`,
3. publishes the folder `docs/` on **GitHub Pages**.

The published page is rendered by `docs/index.html` (Redoc), which reads `./openapi.json`.

## 3) Activation on the GitHub repo's side

In GitHub :

1. `Settings` $\rightarrow$ `Pages`
2. Source : **GitHub Actions**
3. Launches **Publish API docs** workflow

Then the documentation is available on the repo Pages' URL.
