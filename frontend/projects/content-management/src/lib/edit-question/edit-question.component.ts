import {Component, inject, OnInit} from '@angular/core';
import {Form, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Question, QuestionType, QuestionUpdateRequest} from '../../../../shared/src/lib/interfaces/question';
import {KeyValuePipe, NgForOf, NgIf} from '@angular/common';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';

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

  questionForm!: FormGroup;
  questionId!: number;
  isAdmin: boolean = false;
  userId: number | null = null;

  loadError = false;
  errorMessage = '';
  cannotEditQuestion = false;

  readonly QuestionType = QuestionType;
  readonly maxAnswers = 7;

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
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        if (initializedUser) {
          this.userId = initializedUser.id;
          this.isAdmin = initializedUser.isAdmin || false;
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

    if (this.isAdmin) {
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

    if (!this.isAdmin) {
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
    this.answersArray.removeAt(index);
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
      alert('Bitte alles ausfüllen')
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
        alert('Frage wurde aktualisiert');
        this.router.navigate(['/questions/manage']);
      },
      error: err => {
        console.error(err);
        if (err.status === 403) {
          alert(err.error?.error || 'Du hast keine Berechtigung diese Frage zu bearbeiten.');
        } else {
          alert('Fehler beim Aktualisieren der Frage.');
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
