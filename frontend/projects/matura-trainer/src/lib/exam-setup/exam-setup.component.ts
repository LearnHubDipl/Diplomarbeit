import {Component, inject} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'lib-exam-setup',
  imports: [
    ReactiveFormsModule,
    NgForOf
  ],
  templateUrl: './exam-setup.component.html',
  styleUrl: './exam-setup.component.css'
})
export class ExamSetupComponent {
  fb = inject(FormBuilder);
  router = inject(Router);
  activatedRoute: ActivatedRoute = inject(ActivatedRoute);

  // Example topic pools; in real app fetch them from API
  topicPools = [
    { id: 1, name: 'Mathematik' },
    { id: 2, name: 'Physik' },
    { id: 3, name: 'Biologie' }
  ];

  form: FormGroup = this.fb.group({
    userId: [1, Validators.required],
    topicPoolIds: [[], Validators.required],
    numberOfQuestions: [5, [Validators.required, Validators.min(1)]],
    timeLimitMinutes: [30, [Validators.required, Validators.min(1)]]
  });

  startExam() {
    if (this.form.invalid) return;

    const settings = this.form.value;

    this.router.navigate(['exam'], {
      relativeTo: this.activatedRoute,
      state: { settings }
    });
  }
}
