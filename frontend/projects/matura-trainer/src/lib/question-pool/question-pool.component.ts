import {Component, inject} from '@angular/core';
import {NgClass, NgForOf, NgIf} from '@angular/common';
import {Router} from '@angular/router';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionPoolEntry} from '../../../../shared/src/lib/interfaces/question-pool';

/**
 * Currently just the question browser component copied. TODO: make changes necessary for editing the question pool
 */

@Component({
  selector: 'lib-question-pool',
  imports: [
    NgForOf,
    NgIf,
    NgClass
  ],
  templateUrl: './question-pool.component.html',
  styleUrls: [
    './question-pool.component.css',
    '../styles/shared-styles.css'
  ]
})
export class QuestionPoolComponent {
  router: Router = inject(Router);

  subjectService: SubjectService = inject(SubjectService);
  subjects: Subject[] = []

  questionPoolService: QuestionPoolService = inject(QuestionPoolService);
  entries: QuestionPoolEntry[] = []

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

      this.loadQuestionsForUser()
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

  loadQuestionsForUser() {
    this.questionPoolService.getQuestionPoolForUser(1).subscribe(pool => {
      this.entries = pool.entries;
    });
  }

  closeAllQuestionDropDowns() {
    for (let curr of this.entries) {
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
    for (let currQuestion of this.entries) {
      if(!this.selectedQuestionIds.includes(currQuestion.question.id)) {
        this.selectedQuestionIds.push(currQuestion.question.id);
      }
    }
  }
  deSelectAllQuestions() {
    for (let currQuestion of this.entries) {
      this.selectedQuestionIds = this.selectedQuestionIds.filter(id => id !== currQuestion.question.id);
    }
  }

  allQuestionsSelected() :boolean {
    return this.entries.every(q => this.selectedQuestionIds.includes(q.question.id));
  }

  addQuestionsToQuestionPool() {
    // TODO: Make editing question pool functions
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
