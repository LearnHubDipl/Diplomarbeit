import {Component, OnInit, inject} from '@angular/core';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {Router, RouterLink} from '@angular/router';
import {NgForOf, NgClass, NgIf} from '@angular/common';

@Component({
  selector: 'lib-choose-study-topic',
  standalone: true,
  imports: [NgForOf, NgClass, RouterLink, NgIf],
  templateUrl: './choose-study-topic.component.html',
  styleUrls: ['./choose-study-topic.component.css']
})
export class ChooseStudyTopicComponent implements OnInit {
  private subjectService = inject(SubjectService);
  private router = inject(Router);

  subjects: Subject[] = [];
  openSubjectId: number | null = null;

  loadError = false;
  errorMessage = '';

  ngOnInit(): void {
    this.loadError = false;
    this.errorMessage = '';

    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fächer:', err);
        this.loadError = true;

        if (err.status === 0) {
          this.errorMessage = 'Backend nicht erreichbar. Bitte starte den Server.';
        } else {
          this.errorMessage = 'Unbekannter Fehler beim Laden der Fächer.';
        }
      }
    });
  }

  toggleSubject(id: number): void {
    this.openSubjectId = this.openSubjectId === id ? null : id;
  }

  navigateToTopicPool(pool: TopicPool): void {
    this.router.navigate([`/questionCard/${pool.id}`], {
      queryParams: {topicPoolId: pool.id}
    });
  }

  navigateToFragenkonfigurator(subject: Subject, pool: TopicPool) {
    this.router.navigate(['/fragenkonfigurator'], {
      queryParams: {
        subjectId: subject.id,
        topicPoolId: pool.id,
        fromSubjectSelection: true
      }
    });
  }
}
