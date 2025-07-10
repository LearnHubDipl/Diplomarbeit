# Learnhub

This project was generated using [Angular CLI](https://github.com/angular/angular-cli) version 19.1.2.

## Overview

Learnhub is structured as a **modular Angular workspace** that separates responsibilities into independent **feature libraries**. This makes the project more scalable, maintainable, and allows multiple teams to work in parallel.

---

## 📁 Project Structure

learnhub/
├── src/ ← Root Angular App
│ └── app/
│ ├── app.routes.ts         ← Main routes (route hierarchie)
│ └── layouts/              ← Layout wrappers (AppLayout, TrainerLayout)
├── projects/
│ ├── content-management/   ← Library for content management    (components and services exclusive to content management)
│ ├── matura-trainer/       ← Library for trainer feature   (components and services exclusive to the matura trainer)
│ └── shared/               ← Shared library (models, services, ...)


---

## 📦 Libraries

| Library              | Responsibility                          | Maintained by       |
|----------------------|-----------------------------------------|---------------------|
| `content-management` | Pages & UI for managing content         | Team Content        |
| `matura-trainer`     | Trainer feature (questions, scoring...) | Team MaturaTrainer  |
| `shared`             | Common interfaces, services, ...        | Both teams          |

Each library is a full Angular library and is located under `projects/<name>`.

---

## 🧩 Routing Structure

The main app defines the routes in `src/app/app.routes.ts`. We use **two layout components** to separate areas:

- `AppLayoutComponent`: main navigation - (content management)
- `TrainerLayoutComponent`: special layout for trainer mode (separate nav)

Example:

```ts
{
  path: '',
  component: AppLayoutComponent,
  children: [
    { path: '', component: HomeComponent },
    { path: 'about', component: AboutComponent }
  ]
},
{
  path: 'trainer',
  component: TrainerLayoutComponent,
  children: [
    { path: '', component: QuestionRunnerComponent }
  ]
}

In this example there are two main routes defined, AppLayoutComponent in "/" and TrainerLayoutComponent in "/trainer"
Under the **AppLayoutComponent** there are two other routes defined, Home through "/" and About through "/about".
Under **TrainerLayoutComponent** through "/trainer" there is only one route defined, QuestionRunner through "/".

If the user now decides to navigate between home and about, the navigation bar **doesn't change**. But if they decide to go to the trainer route, the navigation changes to what is defined in the **TrainerLayoutComponent**!
Going Back from trainer to content management is simply just another routerlink to "/".

## Development server

To start a local development server, run:

```bash
ng serve
```

Once the server is running, open your browser and navigate to `http://localhost:4200/`. The application will automatically reload whenever you modify any of the source files.


## How To Develop

Since this project is now modular and uses libraries, there are a few differences in how to generate new components, services, ...

Add a new component to the library (e.g add my-component to the matura-trainer library):

```bash
ng generate component my-component --project=matura-trainer
```

You can change matura-trainer here to content-management or shared, this is just the name of the library

Add a new service to shared (e.g service my-helper):

```bash
ng generate service my-helper --project=shared
```

## Additional Resources

For more information on using the Angular CLI, including detailed command references, visit the [Angular CLI Overview and Command Reference](https://angular.dev/tools/cli) page.
