import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { UserInitializationService } from '../../../../shared/src/lib/services/user-initialization.service';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'lib-question-approval-list',
  standalone: true,
  imports: [NgForOf, NgIf, RouterLink, FormsModule],
  templateUrl: './question-approval-list.component.html',
  styleUrl: './question-approval-list.component.css'
})
export class QuestionApprovalListComponent implements OnInit {
  private questionService = inject(QuestionService);
  private subjectService = inject(SubjectService);
  private userInitService = inject(UserInitializationService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  allQuestions: Question[] = [];
  filteredQuestions: Question[] = [];
  subjects: Subject[] = [];

  // Filters
  sortOrder: 'newest' | 'oldest' | 'alphabetical' = 'newest';
  selectedSubjectId: number | null = null;

  isAdmin = false;
  loadError = false;
  errorMessage = '';
  isLoading = true;

  async ngOnInit() {
    await this.checkAdminAccess();
    if (this.isAdmin) {
      this.loadSubjects();
      this.loadPrivateQuestions();
    }
  }

  private async checkAdminAccess() {
    const user = this.userInitService.getCurrentUser();
    if (user) {
      this.isAdmin = user.isAdmin || false;
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        this.isAdmin = initializedUser?.isAdmin || false;
      } catch (err) {
        console.error('Fehler beim Laden des Users:', err);
      }
    }

     if (!this.isAdmin) {
      this.router.navigate(['/']);
      /**this.snackBar.open('Zugriff verweigert: Nur Admins können Fragen genehmigen', 'Schließen', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });**/
    }

  }

  private loadSubjects(): void {
    this.subjectService.getAllSubjects().subscribe({
      next: (data) => {
        this.subjects = data;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fächer:', err);
      }
    });
  }

  private loadPrivateQuestions(): void {
    this.isLoading = true;
    this.questionService.getAllQuestionsWithApprovalRequest().subscribe({
      next: (questions) => {
        this.allQuestions = questions;
        this.applyFilters();
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Fehler beim Laden der Fragen:', err);
        this.loadError = true;
        this.isLoading = false;

        if (err.status === 403) {
          this.errorMessage = 'Zugriff verweigert: Nur Admins können private Fragen sehen';
          this.router.navigate(['/']);
        } else {
          this.errorMessage = 'Fragen konnten nicht geladen werden';
        }
      }
    });
  }

  applyFilters(): void {
    let filtered = [...this.allQuestions];

    // Filter by subject
    if (this.selectedSubjectId !== null) {
      filtered = filtered.filter(q => {
        const subjectId = q.topicPool?.subject?.id;
        return subjectId === this.selectedSubjectId;
      });
    }

    // Sort
    switch (this.sortOrder) {
      case 'newest':
        filtered.sort((a, b) => b.id - a.id);
        break;
      case 'oldest':
        filtered.sort((a, b) => a.id - b.id);
        break;
      case 'alphabetical':
        filtered.sort((a, b) => a.text.localeCompare(b.text));
        break;
    }

    this.filteredQuestions = filtered;
  }

  onSortChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.sortOrder = value as 'newest' | 'oldest' | 'alphabetical';
    this.applyFilters();
  }

  onSubjectChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.selectedSubjectId = value ? Number(value) : null;
    this.applyFilters();
  }

  viewQuestionDetails(questionId: number): void {
    this.router.navigate(['/questions/approve', questionId]);
  }

  getFirstCharacters(text: string, length: number = 80): string {
    if (!text) return '';
    return text.length > length ? text.slice(0, length) + '...' : text;
  }

  getDifficultyLabel(difficulty: number): string {
    switch (difficulty) {
      case 1: return 'Leicht';
      case 2: return 'Mittel';
      case 3: return 'Schwer';
      default: return 'Unbekannt';
    }
  }

  getQuestionTypeLabel(type: string): string {
    switch (type) {
      case 'MULTIPLE_CHOICE': return 'Multiple Choice';
      case 'FREETEXT': return 'Freitext';
      default: return type;
    }
  }
}
