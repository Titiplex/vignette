# Vignette Project

<p align="center">
  <img src="https://img.shields.io/badge/status-active-2ea44f" alt="status">
  <img src="https://img.shields.io/badge/backend-Spring%20Boot-6DB33F" alt="Spring Boot">
  <img src="https://img.shields.io/badge/frontend-Vue%203-42b883" alt="Vue 3">
  <img src="https://img.shields.io/badge/database-PostgreSQL-4169E1" alt="PostgreSQL">
  <img src="https://img.shields.io/badge/docs-OpenAPI-orange" alt="OpenAPI">
  <img src="https://img.shields.io/badge/license-GPLv3-blue" alt="GPLv3">
  <img src="https://img.shields.io/badge/media-audio%20%26%20storyboards-purple" alt="Media">
</p>

<p align="center">
  Storyboard-based platform for language documentation, learning, and audio-rich scenario exploration.
</p>

<p align="center">
  <a href="https://titiplex.github.io/vignette/">API Docs</a>
  ·
  <a href="#getting-started">Getting Started</a>
</p>

**Vignette** is a web platform for **storyboard-based language documentation and learning**.

It lets users create, explore, and manage linguistic content built around scenarios, thumbnails, audio, and
community-oriented features.

<!-- TOC -->
* [Vignette Project](#vignette-project)
  * [Project Information](#project-information)
  * [Overview](#overview)
  * [Goals](#goals)
  * [Technologies](#technologies)
  * [Dependencies](#dependencies)
  * [Structure](#structure)
  * [Links](#links)
  * [Getting started](#getting-started)
    * [Prerequisites](#prerequisites)
    * [Back-End](#back-end)
      * [Building and launching the application](#building-and-launching-the-application)
      * [Test backend code](#test-backend-code)
      * [Package the backend as a jar file](#package-the-backend-as-a-jar-file)
    * [Front-End](#front-end)
      * [Install and launch](#install-and-launch)
      * [Build](#build)
      * [Test frontend code](#test-frontend-code)
  * [Configuration](#configuration)
    * [Development profile](#development-profile)
    * [Production profile](#production-profile)
  * [Environment variables](#environment-variables)
    * [Required or recommended](#required-or-recommended)
    * [Production database variables](#production-database-variables)
  * [API documentation](#api-documentation)
    * [Local API docs](#local-api-docs)
    * [Published API docs](#published-api-docs)
  * [Roadmap ideas](#roadmap-ideas)
  * [Contributing](#contributing)
  * [Security](#security)
  * [License](#license)
  * [Code of Conduct](#code-of-conduct)
<!-- TOC -->

## Project Information

|                  |                                                    |
|------------------|----------------------------------------------------|
| **Title**        | Vignette                                           |
| **Author**       | Titouan JOHANNY                                    |
| **Published**    | false                                              |
| **Universities** | - Université de Montréal</br> - Avignon Université |

## Overview

This project aims at creating storyboards for low resource languages, thus supporting **language documentation,
exploration, and learning through storyboards**.
It is aimed at teaching the language to children, as well as providing a centralized platform for community sharing and
providing tools for linguists.

It was initiated at **_Université de Montréal_** in 2026 by **_Titouan JOHANNY_**, as a part of the class IFT-3150 under
the supervision of **_Mr. Louis-Edouard LAFONTANT_**.

## Goals

- Provide a clean platform for **documenting languages through structured scenarios**
- Support **storyboard-like navigation** through thumbnails and related media
- Enable **audio-backed learning and documentation**
- Offer a backend API that can serve both the web app and external tooling
- Keep the project maintainable through a split frontend/backend architecture

## Technologies

The backend is built in Java with Spring Boot and follows a API REST architecture.
The frontend is built with Vite and Vue.js.
The API docs is made with Swagger and OpenAPI, and published on Github Pages.

## Dependencies

Languages data and metadata are directly imported from [Glottolog](https://glottolog.org)

## Structure

```text
.
├── .github/workflows/       # CI and documentation publication workflows
├── docs/                    # Static API documentation published on GitHub Pages
├── src/                     # Spring Boot backend
│   ├── main/java/org/titiplex/
│   │   ├── api/
│   │   ├── bootstrap/
│   │   ├── config/
│   │   ├── persistence/
│   │   └── service/
│   └── main/resources/
├── vite/                    # Vue frontend
├── pom.xml                  # Maven backend build
└── API_DOCUMENTATION.md     # Notes about API doc generation/publication
```

## Links

- [API docs](https://titiplex.github.io/vignette/)
- [Repository](https://github.com/titiplex/vignette/)
- [Project report](https://protolabo.github.io/vignette/) (in french)

## Getting started

### Prerequisites

Make sure you have the following installed:

- Java 23
- Maven 3.9+
- Node.js 22+
- npm
- PostgreSQL (only needed for production-like setup)

### Back-End

The backend is the API part of the project that powers the web app.

#### Building and launching the application

````shell
# in project root
mvn clean build

# launching the application
mvn spring-boot:run -Dspring-boot.run.profiles=dev
````

> Note: the `-Dspring-boot.run.profiles=dev` flag is only needed for development.

Backend should start on `http://localhost:8081`

Dev endpoints :

- Swagger UI: `http://localhost:8081/api/docs`
- OpenAPI JSON: `http://localhost:8081/api/docs/openapi`
- H2 console: `http://localhost:8081/h2-console`

#### Test backend code

```shell
# in project root
mvn clean test
```

#### Package the backend as a jar file

```shell
mvn clean package
java -jar target/projet-info.jar --spring.profiles.active=dev
```

### Front-End

#### Install and launch

````shell
cd vite # go to the frontend directory
npm install # install the dependencies, skip if already done
npm run dev
````

#### Build

```shell
npm run build
```

To preview the production build:

```shell
npm run preview
```

#### Test frontend code

```bash
cd vite
npm install # install the dependencies, skip if already done
npm run test:run # for single run
npm run test # for watch mode
npm run test:coverage # for coverage report
```

Frontend end to end tests :

````shell
cd vite
npm run e2e

# With playwright UI
npm run e2e:ui
````

## Configuration

### Development profile

The `dev` profile is configured for local development with:

- port `8081`,
- an H2 file database,
- H2 console enabled,
- multipart upload limits,
- Swagger / OpenAPI enabled.

### Production profile

The prod profile is configured for deployment with:

- port from `PORT` (default `8081`),
- PostgreSQL datasource,
- configurable credentials through environment variables.

## Environment variables

### Required or recommended

````dotenv
APP_JWT_SECRET=change-me
APP_STORAGE_ROOT=./data/storage
````

### Production database variables

````dotenv
DB_URL=jdbc:postgresql://localhost:5432/vignette
DB_USERNAME=your_user
DB_PASSWORD=your_password
PORT=8081
````

## API documentation

The backend uses **springdoc-openapi** to generate API documentation automatically.

### Local API docs

Once the backend is running:

- Swagger UI: ``http://localhost:8081/api/docs``
- OpenAPI JSON: ``http://localhost:8081/api/docs/openapi``

### Published API docs

The repository includes a GitHub Actions workflow that:

- builds the backend,
- starts the API,
- exports the **OpenAPI** spec,
- publishes the ``docs/`` folder to GitHub Pages.

If GitHub Pages is enabled for the repository, the published API documentation is available from the project’s Pages
site.

## Roadmap ideas

This project can be extended further with:

- richer admin tooling,
- better media management and storage organization,
- stronger access-control modeling,
- improved contributor onboarding,
- Docker-based local setup,
- seeded demo content,

## Contributing

Contributions are welcome.

A good contribution flow is:

1. Fork the repository
2. Create a dedicated branch
3. Make focused changes
4. Add or update tests
5. Open a pull request

## Security

If you find a vulnerability, please follow the process described in <a href="SECURITY.md">SECURITY.md</a>.

## License

This project is distributed under the **GPL-3.0** license. See <a href="LICENSE">LICENSE</a> for details.

## Code of Conduct

A contributor code of conduct can be found in <a href="CODE_OF_CONDUCT.md">CODE_OF_CONDUCT.md</a>.