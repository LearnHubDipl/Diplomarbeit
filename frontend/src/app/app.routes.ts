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
import {
  QuestionManagerComponent
} from '../../projects/content-management/src/lib/question-manager/question-manager.component';
import {EditQuestionComponent} from '../../projects/content-management/src/lib/edit-question/edit-question.component';
import {CreateSolutionComponent} from '../../projects/matura-trainer/src/lib/create-solution/create-solution.component';
import {
  SubjectDetailComponent
} from '../../projects/content-management/src/lib/subject-detail/subject-detail.component';
import {
  SubjectPoolDetailComponent
} from '../../projects/content-management/src/lib/subject-pool-detail/subject-pool-detail.component';
import {NotFoundComponent} from '../../projects/content-management/src/lib/not-found/not-found.component';
import {
  QuestionApprovalListComponent
} from '../../projects/content-management/src/lib/question-approval-list/question-approval-list.component';
import {
  QuestionApprovalDetailComponent
} from '../../projects/content-management/src/lib/question-approval-detail/question-approval-detail.component';
import {pendingExamGuard} from '../../projects/matura-trainer/src/lib/pending-exam-guard';

export const routes: Routes = [
  {
    path: '',
    component: AppLayoutComponent,
    children: [
      { path: '', component: ContentHomeComponent },
      { path: 'about', component: AboutComponent },
      {
        path: 'study',
        children: [
          { path: 'choose', component: StudyOrCreateComponent },
          { path: 'start', component: StartLearningComponent },
          { path: 'topics', component: ChooseStudyTopicComponent },
          { path: 'card/:id', component: FrageCardComponent },
          { path: 'finished', component: FinishedCardComponent },
        ]
      },

      {
        path: 'questions',
        children: [
          { path: 'create', component: StartCreateComponent },
          { path: 'new', component: FragenKonfiguratorComponent },
          { path: 'edit/:id', component: EditQuestionComponent },
          { path: 'manage', component: QuestionManagerComponent },
          { path: 'approve', component: QuestionApprovalListComponent },
          { path: 'approve/:id', component: QuestionApprovalDetailComponent },
        ]
      },

      { path: 'profile', component: PersonalPlaceComponent },
      { path: 'subjects', component: SubjectsComponent },
      { path: 'subjects/:id', component: SubjectDetailComponent },
      { path: 'subjects/:subjectId/pools/:poolId', component: SubjectPoolDetailComponent },
      { path: 'subjects/:subjectId/pools/by-name/:poolName', component: SubjectPoolDetailComponent },
    ]
  },

  {
    path: 'trainer',
    component: TrainerLayoutComponent,
    children: [
      { path: '', redirectTo: 'practice', pathMatch: 'full' },

      {
        path: 'practice',
        data: { breadcrumb: 'Meinen Fragenpool üben' },
        children: [
          { path: '', component: TrainerHomeComponent, data: { breadcrumb: null } },
          { path: 'quiz', component: PracticeComponent, data: { breadcrumb: 'Fragen beantworten' } },
          { path: 'fragen', component: QuestionBrowserComponent, data: { breadcrumb: 'Fragen browsen' } },
          { path: 'create-solution/:id', component: CreateSolutionComponent, data: { breadcrumb: 'Lösungsweg erstellen' } },
          {
            path: 'setup-exam',
            data: { breadcrumb: 'Prüfung konfigurieren' },
            children: [
              { path: '', component: ExamSetupComponent, data: { breadcrumb: null } },
              { path: 'exam', component: ExamComponent, data: { breadcrumb: 'Prüfungsmodus' }, canDeactivate: [pendingExamGuard] }
            ]
          }
        ]
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
  },
  {
    path: '**',
    component: AppLayoutComponent,
    children: [
      { path: '**', component: NotFoundComponent }
    ]
  }
];
