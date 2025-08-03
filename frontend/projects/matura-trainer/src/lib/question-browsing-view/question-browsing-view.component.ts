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

  ngOnInit() {
    this.subjectService.getAllSubjects().subscribe(subjects => {
      this.subjects = subjects;
      this.subjects.forEach(subject => {
        this.openSubjectDropDowns[subject.id] = false;
      });

      // Restore state
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
        this.saveState(topicPool);
      });
    } else {
      this.questionPoolService.getEntriesByTopicPool(1, topicPool).subscribe(entries => {
        this.entries = entries;
        this.saveState(topicPool);
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

  loadQuestionsForTopicPool(topicPool: TopicPool) {
    this.selectedTopicPool = topicPool;
    this.questionService.getQuestionsByTopicPool(topicPool).subscribe(questions => {
      this.questions = questions;

      // save state for later
      sessionStorage.setItem('questionBrowserState', JSON.stringify({
        openSubjectDropDowns: this.openSubjectDropDowns,
        openQuestionDropDowns: this.openQuestionDropDowns,
        selectedTopicPoolId: topicPool.id
      }));
    });
  }

  closeAllQuestionDropDowns() {
    for (let curr of this.questions) {
      this.openQuestionDropDowns[curr.id] = false;
    }
  }


  toggleSelectionMode() {
    this.selecting = !this.selecting;
    if(this.selecting) {
      this.closeAllQuestionDropDowns()
    } else {
      this.selectedQuestionIds = []
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
    for (let currQuestion of this.questions) {
      if(!this.selectedQuestionIds.includes(currQuestion.id)) {
        this.selectedQuestionIds.push(currQuestion.id);
      }
    }
  }
  deSelectAllQuestions() {
    for (let currQuestion of this.questions) {
      this.selectedQuestionIds = this.selectedQuestionIds.filter(id => id !== currQuestion.id);
    }
  }

  allQuestionsSelected() :boolean {
    return this.questions.every(q => this.selectedQuestionIds.includes(q.id));
  }

  addQuestionsToQuestionPool() {
    let payload: QuestionPoolEntryRequest = {
      userId: 1,
      questionIds: this.selectedQuestionIds
    };
    this.questionPoolService.postQuestionsToQuestionPool(payload).subscribe(p => {
      console.log(p)
      this.selectedQuestionIds = []
      this.selecting = false;
    })
  }


  navigateToQuestionRunner(questionId: number) {
    sessionStorage.setItem('questionBrowserState', JSON.stringify({
      openSubjectDropDowns: this.openSubjectDropDowns,
      openQuestionDropDowns: this.openQuestionDropDowns,
      selectedTopicPoolId: this.selectedTopicPool?.id
    }));

    this.router.navigate(['/trainer/practice/quiz'], {
      queryParams: { ids: [questionId] }
    });
  }
}
