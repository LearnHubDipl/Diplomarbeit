import { Routes } from '@angular/router';
import {HomeComponent as ContentHomeComponent } from '../../projects/content-management/src/lib/home/home.component';
import {HomeComponent as TrainerHomeComponent } from '../../projects/matura-trainer/src/lib/home/home.component';
import {QuestionRunnerComponent} from '../../projects/matura-trainer/src/lib/question-runner/question-runner.component';
import {TrainerLayoutComponent} from './layouts/trainer-layout/trainer-layout.component';
import {AppLayoutComponent} from './layouts/app-layout/app-layout.component';
import {AboutComponent} from '../../projects/content-management/src/lib/about/about.component';
import {StatsHomeComponent} from '../../projects/matura-trainer/src/lib/stats-home/stats-home.component';

export const routes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', component: ContentHomeComponent },
      { path: 'about', component: AboutComponent }
    ]
  },
  {
    path: 'trainer',
    component: TrainerLayoutComponent,
    children: [
      { path: '', redirectTo: 'practice', pathMatch: 'full' },

      {
        path: 'practice',
        data: { breadcrumb: 'Üben' },
        children: [
          { path: '', component: TrainerHomeComponent, data: { breadcrumb: null } }, // home of practice
          { path: 'quiz', component: QuestionRunnerComponent, data: { breadcrumb: 'Fragen beantworten' } }
        ]
      },

      {
        path: 'stats',
        component: StatsHomeComponent,
        data: { breadcrumb: 'Statistik' }
      }
    ]
  }
];
