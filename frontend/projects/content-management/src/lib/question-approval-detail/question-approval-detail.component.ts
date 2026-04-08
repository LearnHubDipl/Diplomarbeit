import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { NgClass, NgForOf, NgIf } from '@angular/common';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { UserInitializationService } from '../../../../shared/src/lib/services/user-initialization.service';
import { Question } from '../../../../shared/src/lib/interfaces/question';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'lib-question-approval-detail',
  standalone: true,
  imports: [NgClass, NgForOf, NgIf, RouterLink],
  templateUrl: './question-approval-detail.component.html',
  styleUrl: './question-approval-detail.component.css'
})
export class QuestionApprovalDetailComponent implements OnInit {
  private questionService = inject(QuestionService);
  private userInitService = inject(UserInitializationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  question: Question | null = null;
  isAdmin = false;
  isLoading = true;
  loadError = false;
  errorMessage = '';

  async ngOnInit() {
    await this.checkAdminAccess();

    if (this.isAdmin) {
      const questionId = Number(this.route.snapshot.paramMap.get('id'));
      if (questionId) {
        this.loadQuestion(questionId);
      } else {
        this.router.navigate(['/questions/approve']);
      }
    }
  }

  private async checkAdminAccess() {
    const user = this.userInitService.getCurrentUser();
    if (user) {
      this.isAdmin = (user.isAdmin || user.isTeacher) || false;
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        this.isAdmin = (initializedUser?.isAdmin || initializedUser?.isTeacher) || false;
      } catch (err) {
        console.error('Fehler beim Laden des Users:', err);
      }
    }

    if (!this.isAdmin) {
      this.router.navigate(['/']);
      this.snackBar.open('Zugriff verweigert: Nur Admins und Lehrer können Fragen genehmigen', 'Schließen', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
    }
  }

  private loadQuestion(id: number): void {
    this.isLoading = true;
    this.questionService.getQuestionById(id).subscribe({
      next: (question) => {
        this.question = question;
        this.isLoading = false;

        // Check if question is already public
        if (question.isPublic) {
          this.snackBar.open('Diese Frage ist bereits öffentlich', 'OK', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top'
          });
        }
      },
      error: (err) => {
        console.error('Fehler beim Laden der Frage:', err);
        this.loadError = true;
        this.isLoading = false;
        this.errorMessage = 'Frage konnte nicht geladen werden';
      }
    });
  }

  approveQuestion(): void {
    if (!this.question) return;

    if (this.question.isPublic) {
      this.snackBar.open('Diese Frage ist bereits öffentlich', 'OK', {
        duration: 3000,
        horizontalPosition: 'right',
        verticalPosition: 'top'
      });
      return;
    }

    const questionId = this.question.id;

    this.questionService.approveQuestion(questionId).subscribe({
      next: () => {
        this.snackBar.open('Frage erfolgreich genehmigt!', 'Schließen', {
          duration: 3000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });

        // Navigate back to list
        this.router.navigate(['/questions/approve']);
      },
      error: (err) => {
        console.error('Fehler beim Genehmigen:', err);
        this.snackBar.open(
          err.error?.error || 'Fehler beim Genehmigen der Frage',
          'Schließen',
          {
            duration: 5000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          }
        );
      }
    });
  }

  backToList(): void {
    this.router.navigate(['/questions/approve']);
  }

  getCreatorClass(): string {
    if (!this.question?.user) {
      return 'N/A';
    }

    return this.question.user.className || 'Keine Klasse';
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
