import {Component, inject, OnInit} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {NgForOf, NgIf} from '@angular/common';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Question} from '../../../../shared/src/lib/interfaces/question';

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
  subjectService: SubjectService = inject(SubjectService);
  subjects: Subject[] = []

  questionService: QuestionService = inject(QuestionService);
  questions: Question[] = [];

  openDropdowns: { [id: number]: boolean } = {};
  selectedTopicPool: TopicPool | null = null;

  ngOnInit() {
    this.subjectService.getAllSubject().subscribe(subjects => {
      this.subjects = subjects
      this.subjects.forEach(subject => {
        this.openDropdowns[subject.id] = false;
      });
    })
  }

  toggleDropdown(id: number): void {
    this.openDropdowns[id] = !this.openDropdowns[id];
  }

  loadQuestionsForTopicPool(topicPool: TopicPool) {
    this.selectedTopicPool = topicPool;
    this.questionService.getQuestionsByTopicPool(topicPool).subscribe(questions => {
      this.questions = questions;
    })
  }
}
