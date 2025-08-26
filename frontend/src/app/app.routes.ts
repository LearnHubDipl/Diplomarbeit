import { Routes } from '@angular/router';

import { HomeComponent as ContentHomeComponent } from '../../projects/content-management/src/lib/home/home.component';
import { HomeComponent as TrainerHomeComponent } from '../../projects/matura-trainer/src/lib/home/home.component';

import { QuestionRunnerComponent } from '../../projects/matura-trainer/src/lib/question-runner/question-runner.component';
import { TrainerLayoutComponent } from './layouts/trainer-layout/trainer-layout.component';
import { AppLayoutComponent } from './layouts/app-layout/app-layout.component';

import { AboutComponent } from '../../projects/content-management/src/lib/about/about.component';
import { StatsHomeComponent } from '../../projects/matura-trainer/src/lib/stats-home/stats-home.component';
import { QuestionBrowserComponent } from '../../projects/matura-trainer/src/lib/question-browser/question-browser.component';
import { StatsTopicsComponent } from '../../projects/matura-trainer/src/lib/stats-topics/stats-topics.component';
import { StatsExamsComponent } from '../../projects/matura-trainer/src/lib/stats-exams/stats-exams.component';

import { StudyOrCreateComponent } from '../../projects/content-management/src/lib/study-or-create/study-or-create.component';
import { StartCreateComponent } from '../../projects/content-management/src/lib/start-create/start-create.component';
import { StartLearningComponent } from '../../projects/content-management/src/lib/start-learning/start-learning.component';
import { FragenKonfiguratorComponent } from '../../projects/content-management/src/lib/fragen-konfigurator/fragen-konfigurator.component';

// aus main:
import { QuestionPoolComponent } from '../../projects/matura-trainer/src/lib/question-pool/question-pool.component';

// aus content-management:
import { PersonalPlaceComponent } from '../../projects/content-management/src/lib/personal-place/personal-place.component';
import { FrageCardComponent } from '../../projects/content-management/src/lib/frage-card/frage-card.component';
import { FinishedCardComponent } from '../../projects/content-management/src/lib/finished-card/finished-card.component';
import { ChooseStudyTopicComponent } from '../../projects/content-management/src/lib/choose-study-topic/choose-study-topic.component';
import { SubjectsComponent } from '../../projects/content-management/src/lib/subjects/subjects.component';
import {PracticeComponent} from '../../projects/matura-trainer/src/lib/practice/practice.component';
import {ExamComponent} from '../../projects/matura-trainer/src/lib/exam/exam.component';
import {ExamSetupComponent} from '../../projects/matura-trainer/src/lib/exam-setup/exam-setup.component';

export const routes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', component: ContentHomeComponent },
      { path: 'about', component: AboutComponent },

      { path: 'chooseStudyOrCreate', component: StudyOrCreateComponent },
      { path: 'startCreate', component: StartCreateComponent },
      { path: 'startStudy', component: StartLearningComponent },
      { path: 'fragenkonfigurator', component: FragenKonfiguratorComponent },

      // content-management neue Seiten:
      { path: 'personalPlace', component: PersonalPlaceComponent },
      { path: 'questionCard/:id', component: FrageCardComponent },
      { path: 'finished', component: FinishedCardComponent },
      { path: 'chooseStudyTopic', component: ChooseStudyTopicComponent },
      { path: 'subjects', component: SubjectsComponent }
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
          { path: 'quiz', component: PracticeComponent, data: { breadcrumb: 'Fragen beantworten' } },
          { path: 'fragen', component: QuestionBrowserComponent, data: { breadcrumb: 'Fragen browsen' } },
          { path: 'question-pool', component: QuestionPoolComponent, data: { breadcrumb: 'Fragenpool bearbeiten' } },
          {
            path: 'setup-exam',
            data: { breadcrumb: 'Prüfung konfigurieren' },
            children: [
              { path: '', component: ExamSetupComponent, data: { breadcrumb: null } },
              { path: 'exam', component: ExamComponent, data: { breadcrumb: 'Prüfungsmodus' } }
            ]
          }        ]
      },

      {
        path: 'stats',
        data: { breadcrumb: 'Statistik' },
        children: [
          { path: 'generell', component: StatsHomeComponent, data: { breadcrumb: 'Generell' } },
          { path: 'themenpool', component: StatsTopicsComponent, data: { breadcrumb: 'Themenpool' } },
          { path: 'pruefungen', component: StatsExamsComponent, data: { breadcrumb: 'Prüfungen' } },
          { path: '', redirectTo: 'generell', pathMatch: 'full' }
        ]
      }
    ]
  }
];
