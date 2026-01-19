<img width="2559" height="1599" alt="image" src="https://github.com/user-attachments/assets/9757ee39-fbb1-4e86-9f16-93c447078daa" />

This repository contains a Java desktop application for managing and scheduling competitive 
tournaments. The application provides tools for creating tournaments, managing participants, 
and generating feasible match schedules under practical constraints.

The project combines algorithmic scheduling with a polished JavaFX user interface, allowing 
users to configure tournaments, generate schedules, and view player stats in real time.

While the project was originally created to manage ping pong tournaments, it may be applied to tournaments in any sport. 

What this project demonstrates:
- Use of greedy algorithms and combinatorial reasoning to construct initial tournament schedules
- Application of recursive backtracking to escape local minima and explore alternative scheduling configurations
- Organization of a Java project using the Maven build and dependency management format
- Design of a stateful JavaFX desktop application with responsive layout and UI reflow to support different window sizes and screen resolutions
- Implementation of drag-and-drop schedule reordering using ghost images

This application was developed primarily as a personal project for the enjoyment of friends and family. 
Below are some instructions for building and running the application:
- The repo contains a Maven wrapper so it is not necessary to install maven globally in order to run the application
- JavaFX is also included as a dependency through Maven so it is not necessary to install JavaFX gobally either
- The only requirement is a JDK version 25 or higher
- The repo contains a tasks.json file with a build configuration to run the program using ctrl + shift + b if VS Code is being used
- If VS Code is not being used the program can be executed with .\mvnw.cmd javafx:run
- Maven and JavaFX will be resolved as dependencies automatically when the program is run for the first time
