import {Component, inject, OnInit} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {Router} from '@angular/router';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {QuestionPoolEntryRequest} from '../../../../shared/src/lib/interfaces/question-pool';

@Component({
  selector: 'lib-question-browser',
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './question-browser.component.html',
  styleUrls: [
    '../styles/shared-styles.css',
    './question-browser.component.css'
  ]
})
export class QuestionBrowserComponent implements OnInit {
  router: Router = inject(Router);

  subjectService: SubjectService = inject(SubjectService);
  subjects: Subject[] = []

  questionService: QuestionService = inject(QuestionService);
  questions: Question[] = [];

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

      // restore states from session storage
      const raw = sessionStorage.getItem('questionBrowserState');
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
              this.loadQuestionsForTopicPool(match);
              break;
            }
          }
        }
      }
    });
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
