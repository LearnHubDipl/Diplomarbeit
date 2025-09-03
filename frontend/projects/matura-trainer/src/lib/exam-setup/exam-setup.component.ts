import {Component, inject, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {CommonModule, NgForOf} from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import { Subject } from '../../../../shared/src/lib/interfaces/subject';
import {SubjectService} from '../../../../shared/src/lib/services/subject.service';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {QuestionPoolService} from '../../../../shared/src/lib/services/question-pool.service';


@Component({
  selector: 'lib-exam-setup',
  imports: [
    ReactiveFormsModule,
    NgForOf,
    CommonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatCheckboxModule,
    FormsModule,
  ],
  templateUrl: './exam-setup.component.html',
  styleUrls: [
    './exam-setup.component.css',
    '../styles/shared-styles.css',
  ]
})
export class ExamSetupComponent implements OnInit {
  fb = inject(FormBuilder);
  router = inject(Router);
  activatedRoute: ActivatedRoute = inject(ActivatedRoute);
  questionPoolService: QuestionPoolService = inject(QuestionPoolService);
  subjectService: SubjectService = inject(SubjectService);
  subjects: Subject[] = []

  form: FormGroup = this.fb.group({
    userId: [1],
    topicPoolIds: [[], Validators.required],
    numberOfQuestions: [5, [Validators.required, Validators.min(1)]],
    timeLimitMinutes: [30, [Validators.required, Validators.min(1)]]
  });

  ngOnInit() {
    // this.subjectService.getAllSubjects().subscribe(subjects => {this.subjects = subjects;});
    this.questionPoolService.getSubjectsForUser(1).subscribe(subjects => this.subjects = subjects);
  }

  startExam() {
    if (this.form.invalid) return;

    this.form.value.userId = 1;
    let settings = this.form.value;

    this.router.navigate(['exam'], {
      relativeTo: this.activatedRoute,
      state: { settings }
    });
  }

  // Check if all topic pools in a subject are selected
  isSubjectSelected(subject: { topicPools: { id: number }[] }) {
    const selected: number[] = this.form.value.topicPoolIds;
    return subject.topicPools.every(pool => selected.includes(pool.id));
  }

  // Toggle all topic pools of a subject
  toggleSubject(subject: { topicPools: { id: number }[] }, checked: boolean) {
    let selected: number[] = [...this.form.value.topicPoolIds];

    if (checked) {
      // Add all topic pools
      subject.topicPools.forEach(pool => {
        if (!selected.includes(pool.id)) selected.push(pool.id);
      });
    } else {
      // Remove all topic pools
      selected = selected.filter(id => !subject.topicPools.some(pool => pool.id === id));
    }

    this.form.patchValue({ topicPoolIds: selected });
  }

  getSelectedSubjects(): string[] {
    const selectedTopicPoolIds: number[] = this.form.value.topicPoolIds;
    return this.subjects
      .filter(subject =>
        subject.topicPools.some(pool => selectedTopicPoolIds.includes(pool.id))
      )
      .map(subject => subject.name);
  }
}
