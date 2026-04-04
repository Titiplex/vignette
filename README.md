---
title: Vignette
author: Titouan JOHANNY
published: true
author_profile: true
universities:
  - Université de Montréal
  - Avignon Université
categories:
  - api
  - website
tags:
  - storyboard
  - low resource language
  - learning
  - linguistics
  - audio
---

# Vignette Project

## Introduction

This project aims at creating storyboards for low resource languages.
It is aimed at teaching the language to children, as well as providing a centralized platform for community sharing and
providing tools for linguists.

It was initiated at **_Université de Montréal_** in 2026 by **_Titouan JOHANNY_**, as a part of the class IFT-3150 under
the supervision of **_Mr. Louis-Edouard LAFONTANT_**.

## Description

## Links

## Technologies

The backend is built in Java with Spring Boot and follows a API REST architecture.
The frontend is built with Vite and Vue.js.
The API docs is made with Swagger and OpenAPI, and published on Github Pages.

## Self installation and Usage

### Back-End (API)

````shell
# in project root
mvn clean install
````

To test backend code:

```shell
# in project root
mvn clean test
```

### Front-End

When in the project directory, you can run:

````shell
cd vite
npm install # install the dependencies, skip if already done
npm run dev
````

To test frontend code:

```bash
cd vite
npm install # install the dependencies, skip if already done
npm run test
npm run test:coverage
```