import { Routes } from '@angular/router';
import {HomeComponent as ContentHomeComponent } from '../../projects/content-management/src/lib/home/home.component';
import {HomeComponent as TrainerHomeComponent } from '../../projects/matura-trainer/src/lib/home/home.component';
import {QuestionRunnerComponent} from '../../projects/matura-trainer/src/lib/question-runner/question-runner.component';
import {TrainerLayoutComponent} from './layouts/trainer-layout/trainer-layout.component';
import {AppLayoutComponent} from './layouts/app-layout/app-layout.component';
import {AboutComponent} from '../../projects/content-management/src/lib/about/about.component';
import {StatsHomeComponent} from '../../projects/matura-trainer/src/lib/stats-home/stats-home.component';
import {
  QuestionBrowserComponent
} from '../../projects/matura-trainer/src/lib/question-browser/question-browser.component';
import {StatsTopicsComponent} from '../../projects/matura-trainer/src/lib/stats-topics/stats-topics.component';
import {StatsExamsComponent} from '../../projects/matura-trainer/src/lib/stats-exams/stats-exams.component';
import {
  StudyOrCreateComponent
} from '../../projects/content-management/src/lib/study-or-create/study-or-create.component';
import {StartCreateComponent} from '../../projects/content-management/src/lib/start-create/start-create.component';
import {
  StartLearningComponent
} from '../../projects/content-management/src/lib/start-learning/start-learning.component';
import {
  FragenKonfiguratorComponent
} from '../../projects/content-management/src/lib/fragen-konfigurator/fragen-konfigurator.component';
import {
  PersonalPlaceComponent
} from '../../projects/content-management/src/lib/personal-place/personal-place.component';
import {FrageCardComponent} from '../../projects/content-management/src/lib/frage-card/frage-card.component';
import {FinishedCardComponent} from '../../projects/content-management/src/lib/finished-card/finished-card.component';

export const routes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', component: ContentHomeComponent },
      { path: 'about', component: AboutComponent },
      {path: 'chooseStudyOrCreate', component: StudyOrCreateComponent},
      {path: 'startCreate', component: StartCreateComponent},
      {path: 'startStudy', component: StartLearningComponent},
      {path: 'fragenkonfigurator', component: FragenKonfiguratorComponent},
      {path: 'personalPlace', component: PersonalPlaceComponent},
      {path: 'questionCard', component: FrageCardComponent},
      {path: 'finished', component: FinishedCardComponent}
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
          { path: '', component: TrainerHomeComponent, data: { breadcrumb: null } },
          { path: 'quiz', component: QuestionRunnerComponent, data: { breadcrumb: 'Fragen beantworten' } },
          { path: 'fragen', component: QuestionBrowserComponent, data: { breadcrumb: 'Fragen browsen' } },
          { path: 'pruefungsmodus', component: QuestionRunnerComponent, data: { breadcrumb: 'Prüfungsmodus' } }
        ]
      },

      {
        path: 'stats',
        data: { breadcrumb: 'Statistik' },
        children: [
          { path: 'generell', component: StatsHomeComponent, data: { breadcrumb: 'Generell' } },
          { path: 'themenpool', component: StatsTopicsComponent, data: { breadcrumb: 'Themenpool' } },
          { path: 'pruefungen', component: StatsExamsComponent, data: { breadcrumb: 'Prüfungen' } },
          { path: '', redirectTo: 'generell', pathMatch: 'full' } // optional default
        ]
      }
    ]
  }
];
