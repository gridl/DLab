/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { Subscription } from 'rxjs/Subscription';

import { LoginModel } from './login.model';
import { AppRoutingService, HealthStatusService, ApplicationSecurityService } from '../core/services';
import { ErrorUtils, HTTP_STATUS_CODES } from '../core/util';
import { DICTIONARY } from '../../dictionary/global.dictionary';

@Component({
  moduleId: module.id,
  selector: 'dlab-login',
  templateUrl: 'login.component.html',
  styleUrls: ['./login.component.css']
})

export class LoginComponent implements OnInit {
  readonly DICTIONARY = DICTIONARY;
  model = new LoginModel('', '');
  error: string;
  loading = false;
  userPattern = '\\w+.*\\w+';

  subscription: Subscription;

  constructor(
    private applicationSecurityService: ApplicationSecurityService,
    private appRoutingService: AppRoutingService,
    private healthStatusService: HealthStatusService,
    private ref: ChangeDetectorRef
  ) {
    this.subscription = this.applicationSecurityService.emitter$
      .subscribe(message => this.error = message);
  }

  ngOnInit() {
    this.applicationSecurityService.isLoggedIn().subscribe(result => {
      this.checkHealthStatusAndRedirect(result);
    });
  }

  login_btnClick() {
    this.error = '';
    this.loading = true;

    this.applicationSecurityService
      .login(this.model)
      .subscribe((result) => {
        if (result) {
          this.checkHealthStatusAndRedirect(result);
          return true;
        }

        return false;
      }, (error) => {
        if (DICTIONARY.cloud_provider === 'azure' && error && error.status === HTTP_STATUS_CODES.FORBIDDEN) {
          window.location.href = error.headers.get('Location');
        } else {
          this.error = ErrorUtils.handleError(error);
          this.loading = false;
        }
      });

    return false;
  }
  loginWithAzure_btnClick() {
    this.appRoutingService.redirectToAzure();
  }

  checkHealthStatusAndRedirect(isLoggedIn) {
    if (isLoggedIn)
      this.healthStatusService.isHealthStatusOk()
        .subscribe(isHealthStatusOk => {
          if (isLoggedIn && !isHealthStatusOk) {
            this.appRoutingService.redirectToHealthStatusPage();
          } else {
            this.appRoutingService.redirectToHomePage();
          }
        });
  }
}

