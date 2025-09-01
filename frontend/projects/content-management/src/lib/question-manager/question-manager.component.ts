import {Component, inject, OnInit} from '@angular/core';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {Router} from '@angular/router';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {Question} from '../../../../shared/src/lib/interfaces/question';
import {NgClass, NgForOf, NgIf, SlicePipe} from '@angular/common';
import {TopicPoolService} from '../../../../shared/src/lib/services/topic-pool.service';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';

@Component({
  selector: 'lib-question-manager',
  imports: [
    NgClass,
    NgForOf,
    NgIf
  ],
  templateUrl: './question-manager.component.html',
  styleUrl: './question-manager.component.css'
})
export class QuestionManagerComponent implements OnInit {
  private subjectService = inject(SubjectService);
  private questionService = inject(QuestionService);

  subjects?: Subject[];
  openSubjectId?: number | null;
  openPoolId?: number | null;

  questionsByPool: {
    [poolId: number]: Question []
  } = {};

  ngOnInit(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: data => this.subjects = data,
      error: err => console.error('Fehler beim Laden der Fächer', err)
    });
  }

  toggleSubject(subjectId: number): void {
    this.openSubjectId = this.openSubjectId === subjectId ? null : subjectId;
  }

  togglePool(pool: TopicPool): void {
    if (this.openPoolId === pool.id) {
      this.openPoolId = null;
      return;
    }

    this.openPoolId = pool.id;

    if (!this.questionsByPool[pool.id]) {
      this.questionService.getQuestionsByTopicPool(pool).subscribe({
        next: (questions) => this.questionsByPool[pool.id] = questions,
        error: (err) => console.error(err)
      });
    }
  }

  getFirstCharacters(text: string) {
    let characterLength = 30;
    if (!text) return '';
    return text.length > characterLength ? text.slice(0, characterLength) + '...' : text;
  }

}
