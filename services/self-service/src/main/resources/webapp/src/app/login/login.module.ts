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

import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MaterialModule } from './../shared/material.module';

import { LoginComponent } from './login.component';
import { LogParamsComponent } from './log-params/log-params.component';

import { CoreModule } from '../core/core.module';

export * from './login.component';
export * from './log-params/log-params.component';

@NgModule({
  imports: [
    FormsModule,
    CommonModule,
    CoreModule,
    MaterialModule
  ],
  declarations: [LoginComponent, LogParamsComponent],
  exports: [LoginComponent]
})
export class LoginModule { }
