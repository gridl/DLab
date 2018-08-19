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

import { Injectable } from '@angular/core';
import { Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';

import { ApplicationServiceFacade } from '.';

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class ManageUngitService {

    constructor(private applicationServiceFacade: ApplicationServiceFacade) { }

    public getGitCreds(): Observable<Response> {
        return this.applicationServiceFacade
        .buildGetGitCreds()
        .map(response => response.json())
        .catch((error: any) => {
            return Observable.throw(
                new Error(`{"status": "${ error.status }", "statusText": "${ error.statusText }", "message": "${ error._body }"}`)
            );
        });
    }

    public updateGitCredentials(data): Observable<Response> {
        return this.applicationServiceFacade
        .buildUpdateGitCredentials(data)
        .map((response: Response) => response)
        .catch((error: any) => {
            return Observable.throw(new Error(`${ error.status } (${ error.statusText }).  ${ error._body }`));
        });
    }
}
