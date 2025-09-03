import {Component, inject, OnInit} from '@angular/core';
import {Form, FormArray, FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {QuestionService} from '../../../../shared/src/lib/services/question.service';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {Question, QuestionType, QuestionUpdateRequest} from '../../../../shared/src/lib/interfaces/question';
import {KeyValuePipe, NgForOf, NgIf} from '@angular/common';

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

 questionForm!: FormGroup;
 questionId!:number;

 readonly QuestionType = QuestionType;
 readonly maxAnswers = 7;

 ngOnInit(): void {
   this.questionId = Number(this.route.snapshot.paramMap.get('id'));
   this.initForm();

   this.questionService.getQuestionById(this.questionId).subscribe({
     next: question => this.patchForm(question),
     error: err => console.error('Fehler beim Laden', err)
   });

   this.questionForm.get('type')?.valueChanges.subscribe(type => {
     this.updateAnswersArray(type);
   });
 }

 private initForm(){
   this.questionForm = this.fb.group({
     text: ['', Validators.required],
     type: ['', Validators.required],
     difficulty: [2],
     explanation: [''],
     isPublic: [true],
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

   const answersArray = this.answersArray;
   answersArray.clear();

   question.answers?.forEach(a =>{
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

 addAnswer(){
   if(this.answersArray.length < this.maxAnswers){
     this.answersArray.push(this.fb.group({
       text: ['', Validators.required],
       isCorrect: [false]
     }));
   }
 }

 removeAnswer(index:number){
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

  onSubmit(){
   if(this.questionForm.invalid){
     this.markAllFieldsAsTouched();
     alert('Bitte alles ausfüllen')
     return;
   }

   const updateRequest: QuestionUpdateRequest = {
     text:this.questionForm.get('text')?.value,
     explanation:this.questionForm.get('explanation')?.value,
     answers: this.answersArray.value.map((a: any) => ({ id: a.id, text: a.text, isCorrect: a.isCorrect })),
     isPublic: this.questionForm.get('isPublic')?.value
   };

   this.questionService.updateQuestion(this.questionId, updateRequest).subscribe({
     next:()=>{
       alert('Frage wurde aktualisiert');
       this.router.navigate(['/manageQuestions']);
     },
     error:err => {
       console.error(err);
       alert('Fehler beim aktualisieren')
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
