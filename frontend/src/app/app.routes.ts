import { Routes } from '@angular/router';
import {HomeComponent} from '../../projects/content-management/src/lib/home/home.component';
import {QuestionRunnerComponent} from '../../projects/matura-trainer/src/lib/question-runner/question-runner.component';
import {TrainerLayoutComponent} from './layouts/trainer-layout/trainer-layout.component';
import {AppLayoutComponent} from './layouts/app-layout/app-layout.component';
import {AboutComponent} from '../../projects/content-management/src/lib/about/about.component';

export const routes: Routes = [
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
];
