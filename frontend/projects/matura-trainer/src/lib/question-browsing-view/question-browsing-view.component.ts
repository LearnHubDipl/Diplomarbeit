import {Component, inject, Input, OnInit} from '@angular/core';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {QuestionPoolEntry, QuestionPoolEntryRequest} from '../../../../shared/src/lib/interfaces/question-pool';
import {Router} from '@angular/router';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

@Component({
  selector: 'lib-question-browsing-view',
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './question-browsing-view.component.html',
  styleUrls: [
    './question-browsing-view.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionBrowsingViewComponent implements OnInit {

  @Input("mode") mode: 'browse' | 'pool' = 'browse';
  questions: Question[] = [];
  entries: QuestionPoolEntry[] = [];

  userService: UserInitializationService = inject(UserInitializationService);

  get viewQuestions(): Question[] {
    return this.mode === 'browse'
      ? this.questions
      : this.entries.map(entry => entry.question);
  }

  router: Router = inject(Router);

  subjectService: SubjectService = inject(SubjectService);
  subjects: Subject[] = []

  questionService: QuestionService = inject(QuestionService);

  questionPoolService: QuestionPoolService = inject(QuestionPoolService);

  openSubjectDropDowns: { [id: number]: boolean } = {};
  openQuestionDropDowns: { [id: number]: boolean } = {};
  selectedTopicPool: TopicPool | null = null;

  selecting = false;
  selectedQuestionIds: number[] = [];

  allViewQuestions: Question[] = [];
  viewedQuestions: Question[] = [];
  showOnlyPool: boolean = false;

  ngOnInit() {
    let userId = this.userService.getCurrentUser()!.id;
    this.questionPoolService.getQuestionPoolForUser(userId).subscribe(pool => {
      this.selectedQuestionIds = pool.entries.map(e => e.question.id);
    });
    this.subjectService.getAllSubjects().subscribe(subjects => {
      this.subjects = subjects;
      this.subjects.forEach(subject => {
        this.openSubjectDropDowns[subject.id] = false;
      });

      const key = this.mode === 'browse' ? 'questionBrowserState' : 'questionPoolBrowserState';
      const raw = sessionStorage.getItem(key);
      if (raw) {
        const savedState = JSON.parse(raw);
        this.openSubjectDropDowns = savedState.openSubjectDropDowns || {};
        this.openQuestionDropDowns = savedState.openQuestionDropDowns || {};

        const topicPoolId = savedState.selectedTopicPoolId;
        if (topicPoolId) {
          for (const subject of this.subjects) {
            const match = subject.topicPools?.find(tp => tp.id === topicPoolId);
            if (match) {
              this.selectedTopicPool = match;
              this.loadTopicPoolData(match);
              break;
            }
          }
        }
      }
    });
  }

  loadTopicPoolData(topicPool: TopicPool) {
    this.selectedTopicPool = topicPool;

    if (this.mode === 'browse') {
      this.questionService.getQuestionsByTopicPool(topicPool).subscribe(questions => {
        this.questions = questions;
        this.allViewQuestions = [...questions];
        this.viewedQuestions = [...questions];
      });
    } else {
      this.questionPoolService.getEntriesByTopicPool(1, topicPool).subscribe(entries => {
        this.entries = entries;
        const questions = entries.map(e => e.question);
        this.allViewQuestions = [...questions];
        this.viewedQuestions = [...questions];
      });
    }
  }

  saveState(topicPool: TopicPool) {
    const key = this.mode === 'browse' ? 'questionBrowserState' : 'questionPoolBrowserState';
    sessionStorage.setItem(key, JSON.stringify({
      openSubjectDropDowns: this.openSubjectDropDowns,
      openQuestionDropDowns: this.openQuestionDropDowns,
      selectedTopicPoolId: topicPool.id
    }));
  }

  toggleSubjectDropdown(id: number): void {
    this.openSubjectDropDowns[id] = !this.openSubjectDropDowns[id];
  }

  toggleQuestionDropdown(id: number): void {
    this.openQuestionDropDowns[id] = !this.openQuestionDropDowns[id];
    if(this.openQuestionDropDowns[id]){
      this.closeAllQuestionDropDowns()
      this.openQuestionDropDowns[id] = true;
    }
  }

  closeAllQuestionDropDowns() {
    for (let curr of this.viewedQuestions) {
      this.openQuestionDropDowns[curr.id] = false;
    }
  }

  toggleSelectionMode() {
    this.selecting = !this.selecting;
    if(this.selecting) {
      this.closeAllQuestionDropDowns()
    }
  }

  toggleQuestionSelected(id: number) {
    if (!this.selectedQuestionIds.includes(id)) {
      this.selectedQuestionIds.push(id);
    } else {
      this.selectedQuestionIds = this.selectedQuestionIds.filter(includedId => includedId !== id);
    }
  }

  selectAllQuestions() {
    for (let currQuestion of this.viewedQuestions) {
      if(!this.selectedQuestionIds.includes(currQuestion.id)) {
        this.selectedQuestionIds.push(currQuestion.id);
      }
    }
  }

  deSelectAllQuestions() {
    for (let currQuestion of this.viewedQuestions) {
      this.selectedQuestionIds = this.selectedQuestionIds.filter(id => id !== currQuestion.id);
    }
  }

  allQuestionsSelected() :boolean {
    if (this.viewedQuestions.length === 0) return false;
    return this.viewedQuestions.every(q => this.selectedQuestionIds.includes(q.id));
  }

  addQuestionsToQuestionPool() {
    let payload: QuestionPoolEntryRequest = {
      userId: this.userService.getCurrentUser()!.id,
      questionIds: this.selectedQuestionIds
    };
    this.questionPoolService.postQuestionsToQuestionPool(payload).subscribe(p => {
      this.selecting = false;
    })
  }

  navigateToQuestionRunner(questionId: number) {
    sessionStorage.setItem('questionBrowserState', JSON.stringify({
      openSubjectDropDowns: this.openSubjectDropDowns,
      openQuestionDropDowns: this.openQuestionDropDowns,
      selectedTopicPoolId: this.selectedTopicPool?.id
    }));

    this.router.navigate(
      ['/trainer/practice/quiz'],
      { state: { questionIdList: [questionId], isReadOnly: true } }
    );
  }

  toggleQuestionPool(questionId: number) {
    const userId = this.userService.getCurrentUser()!.id;
    const request = { userId, questionIds: [questionId] };

    if (this.selectedQuestionIds.includes(questionId)) {
      this.questionPoolService.removeQuestionsFromPool(request).subscribe(() => {
        this.selectedQuestionIds = this.selectedQuestionIds.filter(id => id !== questionId);
      });
    } else {
      this.questionPoolService.postQuestionsToQuestionPool(request).subscribe(() => {
        this.selectedQuestionIds.push(questionId);
      });
    }
  }

  toggleShowOnlyPool() {
    if (!this.showOnlyPool) {
      this.viewedQuestions = this.allViewQuestions.filter(q => this.selectedQuestionIds.includes(q.id));
      this.showOnlyPool = true;
    } else {
      this.viewedQuestions = [...this.allViewQuestions];
      this.showOnlyPool = false;
    }
  }


  resetSelection() {
    this.selectedTopicPool = null;
    this.viewedQuestions = [];
  }

  handleBulkAction() {
    const currentIds = this.viewedQuestions.map(q => q.id);
    const userId = this.userService.getCurrentUser()!.id;

    const request: QuestionPoolEntryRequest = {
      userId: userId,
      questionIds: currentIds
    };

    if (!this.allQuestionsSelected()) {
      this.questionPoolService.postQuestionsToQuestionPool(request).subscribe(() => {
        currentIds.forEach(id => {
          if (!this.selectedQuestionIds.includes(id)) {
            this.selectedQuestionIds.push(id);
          }
        });
      });
    } else {
      this.questionPoolService.removeQuestionsFromPool(request).subscribe(() => {
        this.selectedQuestionIds = this.selectedQuestionIds.filter(
          id => !currentIds.includes(id)
        );
      });
    }
  }
}
