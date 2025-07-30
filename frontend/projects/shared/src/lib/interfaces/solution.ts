export interface Solution {
  id: number;
  steps: SolutionStep[];
  upVotes: number;
}

export interface SolutionStep {
  id: number;
  title: string;
  text: string;
}
