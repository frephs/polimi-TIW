# TIW project 2025

[![Full Project Presentation](https://img.shields.io/badge/Docs-Project%20Documentation-blue?logo=markdown)](https://frephs.github.io/polimi-TIW/)



This repository contains my implementation of the web application project for the "Tecnologie Informatiche per il Web" (TIW) course at Politecnico di Milano, taught by Professor Pietro Fraternali.



## Overview and Technology Stack
The project is a web-based auction system where users can upload, sell, and bid on groups of items. It is developed with the following technologies:
- **Frontend**: HTML, CSS, Typescript,
- **Backend**: Java Servlets, MySQL, Apache Tomcat, Thymeleaf 
- **Documentation**: Markdown, PlantUML, Mermaid, Reveal.js, TeX, GitHub Actions

## 🚧 Room for improvement includes:
- [ ] Input sanitization and validation
- [ ] Enhanced security measures: directives and best practices for XSS, CSRF, and CORS
- [ ] Improved error handling and logging server-side
- [ ] Database connection pooling for performance
- [ ] Better cookie management: restrict it and tie to user identity
- [ ] More comprehensive unit and integration tests
- [ ] Use separate connections configuration for different operations (e.g., read vs. write)
- [ ] Make sure all operations are proprerly transactional
- [ ] Make sure there is no user supplied data in the errors.
- [ ] No debugging information in production. 
- [ ] Salt passwords before hashing them in the database.

Two versions of the application are provided:
- A basic multi-page version without JavaScript
- An advanced rich internet application developed as a single-page application version using JavaScript and AJAX requests.

## Project documentation
The project documentation is available in the `docs` branch. It includes:
- A detailed description of the project requirements
- A description of the technologies used
- A description of the database schema
- A description of the web application architecture

