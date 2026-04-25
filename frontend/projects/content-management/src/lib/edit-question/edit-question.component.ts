import {Component, inject, OnInit} from '@angular/core';
import {Form, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Question, QuestionType, QuestionUpdateRequest} from '../../../../shared/src/lib/interfaces/question';
import {KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'lib-edit-question',
  imports: [
    ReactiveFormsModule,
    NgForOf,
    KeyValuePipe,
    NgIf,
    RouterLink
  ],
  templateUrl: './edit-question.component.html',
  styleUrl: './edit-question.component.css'
})
export class EditQuestionComponent implements OnInit {

  private fb = inject(FormBuilder);
  private questionService = inject(QuestionService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private userInitService = inject(UserInitializationService);
  private snackBar = inject(MatSnackBar);

  questionForm!: FormGroup;
  questionId!: number;
  isAdmin: boolean = false;
  isTeacher: boolean = false;
  userId: number | null = null;

  loadError = false;
  errorMessage = '';
  cannotEditQuestion = false;

  readonly QuestionType = QuestionType;
  readonly maxAnswers = 7;
  readonly minAnswers = 2;

  readonly questionTypes = [
    { value: QuestionType.FREETEXT, label: 'Freitext' },
    { value: QuestionType.MULTIPLE_CHOICE, label: 'Multiple Choice' }
  ];

  async ngOnInit() {
    this.questionId = Number(this.route.snapshot.paramMap.get('id'));
    this.initForm();
    await this.loadCurrentUser();
    this.loadQuestion();

    this.questionForm.get('type')?.valueChanges.subscribe(type => {
      this.updateAnswersArray(type);
    });
  }

  private async loadCurrentUser() {
    const user = this.userInitService.getCurrentUser();

    if (user?.id) {
      this.userId = user.id;
      this.isAdmin = user.isAdmin || false;
      this.isTeacher = user.isTeacher || false;
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        if (initializedUser) {
          this.userId = initializedUser.id;
          this.isAdmin = initializedUser.isAdmin || false;
          this.isTeacher = initializedUser.isTeacher || false;
        }
      } catch (error) {
        console.error('Error loading user:', error);
      }
    }
  }

  private loadQuestion() {
    this.questionService.getQuestionById(this.questionId).subscribe({
      next: question => {
        if (this.canAccessQuestion(question)) {
          this.patchForm(question);
        } else {
          this.cannotEditQuestion = true;
          this.errorMessage = this.isAdmin
            ? 'Du kannst nur deine eigenen oder öffentliche Fragen bearbeiten.'
            : 'Du kannst nur deine eigenen Fragen bearbeiten.';
        }
      },
      error: err => {
        console.error('Fehler beim Laden', err);
        this.loadError = true;
        this.errorMessage = 'Frage konnte nicht geladen werden.';
      }
    });
  }

  private canAccessQuestion(question: Question): boolean {
    if (!this.userId) return false;
    const isOwnQuestion = question.user?.id === this.userId;
    const isPublicQuestion = question.isPublic === true;
    if (this.isAdmin || this.isTeacher) {
      return isOwnQuestion || isPublicQuestion;
    }
    return isOwnQuestion;
  }

  private initForm() {
    this.questionForm = this.fb.group({
      text: ['', Validators.required],
      type: ['', Validators.required],
      difficulty: [2],
      explanation: [''],
      isPublic: [false],
      answers: this.fb.array([])
    });
  }

  private patchForm(question: Question) {
    this.questionForm.patchValue({
      text: question.text,
      type: question.type,
      difficulty: question.difficulty,
      explanation: question.explanation,
      isPublic: question.isPublic
    });

    this.questionForm.get('type')?.disable();
    this.questionForm.get('difficulty')?.disable();

    if (!this.isAdmin && !this.isTeacher) {
      this.questionForm.get('isPublic')?.disable();
    }

    const answersArray = this.answersArray;
    answersArray.clear();

    question.answers?.forEach(a => {
      answersArray.push(this.fb.group({
        id: [a.id],
        text: [a.text, Validators.required],
        isCorrect: [a.isCorrect]
      }));
    })
  }

  get answersArray() {
    return this.questionForm.get('answers') as FormArray;
  }

  addAnswer() {
    if (this.answersArray.length < this.maxAnswers) {
      this.answersArray.push(this.fb.group({
        text: ['', Validators.required],
        isCorrect: [false]
      }));
    }
  }

  removeAnswer(index: number) {
    if (this.answersArray.length > this.minAnswers) {
      this.answersArray.removeAt(index);
    }
  }

  private updateAnswersArray(type: QuestionType) {
    if (type !== QuestionType.MULTIPLE_CHOICE) {
      this.answersArray.clear();
    } else if (this.answersArray.length === 0) {
      for (let i = 0; i < 2; i++) {
        this.answersArray.push(this.fb.group({
          text: ['', Validators.required],
          isCorrect: [i === 0]
        }));
      }
    }
  }

  onSubmit() {
    if (this.questionForm.invalid) {
      this.markAllFieldsAsTouched();
      this.snackBar.open('Bitte alles ausfüllen', 'Schließen', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
      return;
    }

    const formValue = this.questionForm.getRawValue();

    const updateRequest: QuestionUpdateRequest = {
      text: formValue.text,
      explanation: formValue.explanation,
      answers: this.answersArray.value.map((a: any) => ({id: a.id, text: a.text, isCorrect: a.isCorrect})),
      isPublic: formValue.isPublic
    };

    this.questionService.updateQuestion(this.questionId, updateRequest).subscribe({
      next: () => {
        this.snackBar.open('Frage wurde aktualisiert', 'Schließen', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['success-snackbar']
        });
        this.router.navigate(['/questions/manage']);
      },
      error: err => {
        console.error(err);
        if (err.status === 403) {
          this.snackBar.open(err.error?.error || 'Du hast keine Berechtigung diese Frage zu bearbeiten.', 'OK', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
        } else {
          this.snackBar.open('Fehler beim Aktualisieren der Frage.', 'OK', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });
        }
      }
    })
  }

  private markAllFieldsAsTouched() {
    Object.keys(this.questionForm.controls).forEach(key => {
      const control = this.questionForm.get(key);
      control?.markAsTouched();
      if (control instanceof FormArray) {
        control.controls.forEach(c => c.markAsTouched());
      }
    });
  }
}
