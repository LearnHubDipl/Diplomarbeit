import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Solution} from '../interfaces/solution';
import { API_BASE_URL } from "./globals";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  constructor(private http: HttpClient) {}

  upvote(solutionId: number, userId: number): Observable<any> {
    return this.http.post(`${API_BASE_URL}/solutions/${solutionId}/votes/up?userId=${userId}`, null);
  }

  downvote(solutionId: number, userId: number): Observable<any> {
    return this.http.post(`${API_BASE_URL}/solutions/${solutionId}/votes/down?userId=${userId}`, null);
  }

  getVoteCount(solutionId: number): Observable<{solutionId: number, upVotes: number, downVotes: number}> {
    return this.http.get<{solutionId: number, upVotes: number, downVotes: number}>(
      `${API_BASE_URL}/solutions/${solutionId}/votes/count`
    );
  }

  createSolution(questionId: number, userId: number, solution: any): Observable<any> {
    const params = new HttpParams()
      .set('questionId', questionId.toString())
      .set('userId', userId.toString());
    return this.http.post(`${API_BASE_URL}/solutions/create`, solution, { params });
  }

}
