import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { QuestionService } from '../../../../shared/src/lib/services/question.service';
import { SubjectService } from '../../../../shared/src/lib/services/subject.service';
import { TopicPoolService } from '../../../../shared/src/lib/services/topic-pool.service';
import { QuestionType } from '../../../../shared/src/lib/interfaces/question';
import { QuestionRequest, AnswerCreationRequest } from '../../../../shared/src/lib/interfaces/question-creation-request';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import { TopicPool } from '../../../../shared/src/lib/interfaces/topic-pool';

@Component({
  selector: 'lib-fragen-konfigurator',
  imports: [
    ReactiveFormsModule,
    CommonModule
  ],
  templateUrl: './fragen-konfigurator.component.html',
  styleUrl: './fragen-konfigurator.component.css'
})
export class FragenKonfiguratorComponent implements OnInit {
  private fb = inject(FormBuilder);
  private questionService = inject(QuestionService);
  private subjectService = inject(SubjectService);
  private topicPoolService = inject(TopicPoolService);

  questionForm!: FormGroup;
  subjects: Subject[] = [];
  topicPools: TopicPool[] = [];
  questionTypes = Object.values(QuestionType);

  readonly QuestionType = QuestionType;
  readonly maxAnswers = 7; //maximale Anzahl der Antwormöglichkeiten (bei Multiple choice)
  readonly automaticAnswersLoaded = 2;

  ngOnInit() {
    this.initForm();
    this.loadSubjects();
    this.setupFormSubscriptions();
  }

  private initForm() {
    this.questionForm = this.fb.group({
      type: ['', Validators.required],
      subjectId: [null, Validators.required], // null statt leerer String für number
      topicPoolId: [{value: null, disabled: true}, Validators.required], // Disabled per default
      text: ['', Validators.required],
      difficulty: [2], // Default: mittel
      explanation: [''],
      isPublic: [true],
      answers: this.fb.array([])
    });
  }

  private setupFormSubscriptions() {
    // Überwache Änderungen am Fragentyp
    this.questionForm.get('type')?.valueChanges.subscribe(type => {
      this.updateAnswersArray(type);
      this.updateExplanationValidation(type);
    });

    // Überwache Änderungen am Schulfach
    this.questionForm.get('subjectId')?.valueChanges.subscribe(subjectId => {
      const topicPoolControl = this.questionForm.get('topicPoolId');

      if (subjectId) {
        this.loadTopicPoolsForSubject(Number(subjectId));
        // Reset TopicPool Auswahl und aktiviere das Feld
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
          isCorrect: [i === 0] // Erste Antwort standardmäßig richtig
        }));
      }
    }
    // Für FREETEXT werden keine Antworten benötigt
  }

  private updateExplanationValidation(type: QuestionType) {
    const explanationControl = this.questionForm.get('explanation');

    if (type === QuestionType.FREETEXT) {
      // Für FREETEXT ist Erklärung Pflicht
      explanationControl?.setValidators([Validators.required]);
    } else {
      // Für MULTIPLE_CHOICE ist Erklärung optional
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
        console.log('Subjects geladen:', this.subjects);
      },
      error: (error) => {
        console.error('Fehler beim Laden der Schulfächer:', error);
      }
    });
  }

  private loadTopicPoolsForSubject(subjectId: number) {
    console.log('Lade TopicPools für Subject ID:', subjectId);

    // Finde das ausgewählte Subject und verwende dessen TopicPools
    const selectedSubject = this.subjects.find(subject => subject.id === subjectId);

    if (selectedSubject && selectedSubject.topicPools) {
      this.topicPools = selectedSubject.topicPools;
      console.log('TopicPools aus Subject geladen:', this.topicPools);
    } else {
      console.warn('Keine TopicPools für Subject ID gefunden:', subjectId);
      this.topicPools = [];
    }
  }


  onSubmit() {
    if (this.questionForm.valid) {
      const formValue = this.questionForm.value;

      const questionRequest: QuestionRequest = {
        text: formValue.text,
        explanation: formValue.explanation || '',
        type: formValue.type,
        difficulty: formValue.difficulty,
        isPublic: formValue.isPublic,
        userId: 1, // TODO: Aktuelle User-ID aus Authentication Service holen
        topicPoolId: Number(formValue.topicPoolId),
        answers: formValue.answers || []
      };

      this.questionService.createQuestion(questionRequest).subscribe({
        next: () => {
          alert('Frage wurde erfolgreich veröffentlicht!');
          this.reinitForm();
        },
        error: (error) => {
          console.error('Fehler beim Erstellen der Frage:', error);
          alert('Fehler beim Veröffentlichen der Frage. Bitte versuchen Sie es erneut.');
        }
      });
    } else {
      this.markAllFieldsAsTouched();
      alert('Bitte füllen Sie alle Pflichtfelder aus.');
    }
  }

  private reinitForm(){
    const prevSubjectId = this.questionForm.get('subjectId')?.value;
    const prevTopicPoolId = this.questionForm.get('topicPoolId')?.value;

    this.initForm();
    this.setupFormSubscriptions();

    if(prevSubjectId){
      this.questionForm.get('subjectId')?.setValue(prevSubjectId);
      this.loadTopicPoolsForSubject(Number(prevTopicPoolId));
      if(prevSubjectId ){
        this.questionForm.get('topicPoolId')?.setValue(prevTopicPoolId);
        this.questionForm.get('topicPoolId')?.enable();
      }
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
