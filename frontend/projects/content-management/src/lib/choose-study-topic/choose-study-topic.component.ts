import { Component, OnInit, inject } from '@angular/core';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';
import { Router } from '@angular/router';
import { NgForOf, NgClass } from '@angular/common';

@Component({
  selector: 'lib-choose-study-topic',
  standalone: true,
  imports: [ NgForOf, NgClass],
  templateUrl: './choose-study-topic.component.html',
  styleUrls: ['./choose-study-topic.component.css']
})
export class ChooseStudyTopicComponent implements OnInit {
  private subjectService = inject(SubjectService);
  private router = inject(Router);

  subjects: Subject[] = [];
  openSubjectId: number | null = null;

  ngOnInit(): void {
    this.subjectService.getAllSubject().subscribe({
      next: (data) => this.subjects = data,
      error: (err) => console.error('Fehler beim Laden der Fächer:', err)
    });
  }

  toggleSubject(id: number): void {
    this.openSubjectId = this.openSubjectId === id ? null : id;
  }

  navigateToTopicPool(pool: TopicPool): void {
    this.router.navigate([`/questionCard/${pool.id}`], {
      queryParams: { topicPoolId: pool.id }
    });
  }
}
