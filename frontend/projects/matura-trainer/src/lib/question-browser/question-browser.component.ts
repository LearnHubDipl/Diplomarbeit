import {Component, inject, OnInit} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {NgForOf, NgIf} from '@angular/common';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {Router} from '@angular/router';

@Component({
  selector: 'lib-question-browser',
  imports: [
    NgForOf,
    NgIf
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

  openSubjectDropDowns: { [id: number]: boolean } = {};
  openQuestionDropDowns: { [id: number]: boolean } = {};
  selectedTopicPool: TopicPool | null = null;

  ngOnInit() {
    this.subjectService.getAllSubject().subscribe(subjects => {
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
      for (let i = 0; i < this.questions.length; i++) {
        let currId = this.questions[i].id;
        if (id !== currId) this.openQuestionDropDowns[currId] = false;
      }
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
