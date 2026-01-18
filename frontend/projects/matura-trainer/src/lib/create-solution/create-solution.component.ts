import { Component, OnInit, inject, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SolutionService } from '../../../../shared/src/lib/services/solution.service';

@Component({
  selector: 'app-create-solution',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './create-solution.component.html',
  styleUrls: ['./create-solution.component.css']
})
export class CreateSolutionComponent implements OnInit {
  @Input() questionId!: number;
  @Output() close = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  private solutionService = inject(SolutionService);

  form: FormGroup = this.fb.group({
    steps: this.fb.array([])
  });

  ngOnInit() {
    this.addStep();
  }

  get steps() {
    return this.form.get('steps') as FormArray;
  }

  addStep() {
    this.steps.push(this.fb.group({
      title: ['', Validators.required],
      text: ['', Validators.required]
    }));
  }

  removeStep(index: number) {
    if (this.steps.length > 1) this.steps.removeAt(index);
  }

  save() {
    if (this.form.valid && this.questionId) {
      this.solutionService.createSolution(this.questionId, 1, this.form.value).subscribe({
        next: () => this.close.emit(),
        error: (err) => console.error(err)
      });
    }
  }

  cancel() {
    this.close.emit();
  }
}
