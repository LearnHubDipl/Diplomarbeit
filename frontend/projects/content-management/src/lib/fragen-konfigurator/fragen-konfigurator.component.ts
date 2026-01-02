import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule} from '@angular/forms';
import {CommonModule} from '@angular/common';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {UserInitializationService} from '../../../../shared/src/lib/services/user-initialization.service';
import {QuestionType} from '../../../../shared/src/lib/interfaces/question';
import {QuestionRequest, AnswerCreationRequest} from '../../../../shared/src/lib/interfaces/question-creation-request';
import {Subject} from '../../../../shared/src/lib/interfaces/subject';
import {TopicPool} from '../../../../shared/src/lib/interfaces/topic-pool';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'lib-fragen-konfigurator',
  imports: [
    ReactiveFormsModule,
    CommonModule,
    RouterLink
  ],
  templateUrl: './fragen-konfigurator.component.html',
  styleUrl: './fragen-konfigurator.component.css'
})
export class FragenKonfiguratorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private questionService = inject(QuestionService);
  private subjectService = inject(SubjectService);
  private userInitService = inject(UserInitializationService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  questionForm!: FormGroup;
  subjects: Subject[] = [];
  topicPools: TopicPool[] = [];
  questionTypes = Object.values(QuestionType);
  fromSubjectSelection: boolean = false;

  readonly QuestionType = QuestionType;
  readonly maxAnswers = 7;
  readonly automaticAnswersLoaded = 2;

  private subjectId?: number;
  private topicPoolId?: number;
  private currentUserId: number | null = null;
  isAdmin: boolean = false;

  async ngOnInit() {
    this.initForm();
    this.setupFormSubscriptions();
    await this.loadCurrentUser();

    this.route.queryParams.subscribe(params => {
      this.subjectId = params['subjectId'] ? Number(params['subjectId']) : undefined;
      this.topicPoolId = params['topicPoolId'] ? Number(params['topicPoolId']) : undefined;

      const isPublicParam = params['isPublic'] === 'true';

      this.fromSubjectSelection = params['fromSubjectSelection'] === 'true';
      if (this.subjectId) {
        this.questionForm.get('subjectId')?.setValue(this.subjectId);
        this.loadSubjects();
      } else {
        this.loadSubjects();
      }

      if (isPublicParam && this.isAdmin) {
        this.questionForm.get('isPublic')?.setValue(true);
      }
    });
  }

  private async loadCurrentUser() {
    const user = this.userInitService.getCurrentUser();

    if (user?.id) {
      this.currentUserId = user.id;
      this.isAdmin = user.isAdmin || false;
      console.log('Current user ID loaded:', this.currentUserId);
      console.log('Is admin:', this.isAdmin);

      // Disable isPublic toggle if not admin
      if (!this.isAdmin) {
        this.questionForm.get('isPublic')?.disable();
      }
    } else {
      try {
        const initializedUser = await this.userInitService.initializeUser();
        if (initializedUser) {
          this.currentUserId = initializedUser.id;
          this.isAdmin = initializedUser.isAdmin || false;
          console.log('Current user ID loaded:', this.currentUserId);
          console.log('Is admin:', this.isAdmin);

          if (!this.isAdmin) {
            this.questionForm.get('isPublic')?.disable(); // Disable toggle
          }
        }
      } catch (error) {
        console.error('Error loading user:', error);
      }
    }
  }

  private initForm() {
    this.questionForm = this.fb.group({
      type: ['', Validators.required],
      subjectId: [null, Validators.required],
      topicPoolId: [{value: null, disabled: true}, Validators.required],
      text: ['', Validators.required],
      difficulty: [2],
      explanation: [''],
      isPublic: [false],
      approvalRequested: [false],
      answers: this.fb.array([])
    });
  }

  private setupFormSubscriptions() {
    this.questionForm.get('type')?.valueChanges.subscribe(type => {
      this.updateAnswersArray(type);
      this.updateExplanationValidation(type);
    });

    this.questionForm.get('subjectId')?.valueChanges.subscribe(subjectId => {
      const topicPoolControl = this.questionForm.get('topicPoolId');

      if (subjectId) {
        this.loadTopicPoolsForSubject(Number(subjectId));
        topicPoolControl?.setValue(null);
        topicPoolControl?.enable();
      } else {
        this.topicPools = [];
        topicPoolControl?.setValue(null);
        topicPoolControl?.disable();
      }
    });
  }

  private updateAnswersArray(type: QuestionType) {
    const answersArray = this.questionForm.get('answers') as FormArray;
    answersArray.clear();

    if (type === QuestionType.MULTIPLE_CHOICE) {
      for (let i = 0; i < this.automaticAnswersLoaded; i++) {
        answersArray.push(this.fb.group({
          text: ['', Validators.required],
          isCorrect: [i === 0]
        }));
      }
    }
  }

  private updateExplanationValidation(type: QuestionType) {
    const explanationControl = this.questionForm.get('explanation');
    if (type === QuestionType.FREETEXT) {
      explanationControl?.setValidators([Validators.required]);
    } else {
      explanationControl?.clearValidators();
    }

    explanationControl?.updateValueAndValidity();
  }

  get answersArray() {
    return this.questionForm.get('answers') as FormArray;
  }

  private loadSubjects() {
    this.subjectService.getAllSubjects().subscribe({
      next: (subjects) => {
        this.subjects = subjects;

        if (this.subjectId) {
          this.questionForm.get('subjectId')?.setValue(this.subjectId);
          this.loadTopicPoolsForSubject(this.subjectId);

          if (this.topicPoolId) {
            this.questionForm.get('topicPoolId')?.setValue(this.topicPoolId);
            this.questionForm.get('topicPoolId')?.enable();
          }
        }
      },
      error: (error) => {
        console.error('Fehler beim Laden der Schulfächer:', error);
        this.snackBar.open('Fehler beim Laden der Schulfächer', 'Schließen', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['error-snackbar']
        });
      }
    });
  }

  private loadTopicPoolsForSubject(subjectId: number) {
    const selectedSubject = this.subjects.find(subject => subject.id === subjectId);

    if (selectedSubject && selectedSubject.topicPools) {
      this.topicPools = selectedSubject.topicPools;
    } else {
      this.topicPools = [];
    }
  }

  onSubmit() {
    if (!this.currentUserId) {
      this.snackBar.open('Benutzer-ID konnte nicht geladen werden. Bitte laden Sie die Seite neu.', 'Schließen', {
        duration: 5000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['error-snackbar']
      });
      console.error('Current user ID is not available');
      return;
    }

    if (this.questionForm.valid) {
      const formValue = this.questionForm.getRawValue();

      let isPublic = false;
      let approvalRequested = false;

      if (this.isAdmin) {
        isPublic = formValue.isPublic || false;
        approvalRequested = false;
      } else {
        isPublic = false;
        approvalRequested = formValue.approvalRequested || false;
      }

      const questionRequest: QuestionRequest = {
        text: formValue.text,
        explanation: formValue.explanation || '',
        type: formValue.type,
        difficulty: formValue.difficulty,
        isPublic: formValue.isPublic,
        approvalRequested: approvalRequested,
        userId: this.currentUserId,
        topicPoolId: Number(formValue.topicPoolId),
        answers: formValue.answers || []
      };

      console.log('Submitting question with user ID:', this.currentUserId);
      console.log('Is public:', questionRequest.isPublic);

      this.questionService.createQuestion(questionRequest).subscribe({
        next: () => {
          this.snackBar.open('Frage wurde erfolgreich veröffentlicht!', 'OK', {
            duration: 3000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['success-snackbar']
          });

          this.router.navigate([], {
            relativeTo: this.route,
            queryParams: {
              subjectId: this.questionForm.get('subjectId')?.value,
              topicPoolId: this.questionForm.get('topicPoolId')?.value,
              fromSubjectSelection: this.fromSubjectSelection
            },
            queryParamsHandling: 'merge'
          });

          this.initForm();
          this.setupFormSubscriptions();

          if (!this.isAdmin) {
            this.questionForm.get('isPublic')?.disable();
          }
        },
        error: (error) => {
          console.error('Fehler beim Erstellen der Frage:', error);
          this.snackBar.open('Fehler beim Veröffentlichen der Frage. Bitte versuchen Sie es erneut.', 'Schließen', {
            duration: 5000,
            horizontalPosition: 'right',
            verticalPosition: 'top',
            panelClass: ['error-snackbar']
          });
        }
      });
    } else {
      this.markAllFieldsAsTouched();
      this.snackBar.open('Bitte füllen Sie alle Pflichtfelder aus.', 'OK', {
        duration: 4000,
        horizontalPosition: 'right',
        verticalPosition: 'top',
        panelClass: ['warning-snackbar']
      });
    }
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

  getQuestionTypeDisplayName(type: QuestionType): string {
    switch (type) {
      case QuestionType.MULTIPLE_CHOICE:
        return 'Multiple Choice';
      case QuestionType.FREETEXT:
        return 'Freitext';
      default:
        return type;
    }
  }

  addAnswer() {
    const answersArray = this.answersArray;

    if (answersArray.length < this.maxAnswers) {
      answersArray.push(this.fb.group({
        text: ['', Validators.required],
        isCorrect: [false]
      }));
    }
  }
}
