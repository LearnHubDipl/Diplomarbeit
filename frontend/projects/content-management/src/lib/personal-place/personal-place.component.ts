import {Component, inject, OnInit} from '@angular/core';
import {UserService} from '../../../../shared/src/lib/services/user.service';
import { logout } from '../../../../../src/main';
@Component({
  selector: 'lib-personal-place',
  imports: [],
  templateUrl: './personal-place.component.html',
  styleUrl: './personal-place.component.css'
})
export class PersonalPlaceComponent implements OnInit{
    ngOnInit(): void {
        throw new Error("Method not implemented.");
    }
    userService: UserService = inject(UserService)
    protected readonly logout = logout;
}
