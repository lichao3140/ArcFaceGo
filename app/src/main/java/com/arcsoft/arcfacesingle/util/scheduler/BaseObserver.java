 /**
 * Copyright 2020 ArcSoft Corporation Limited. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package com.arcsoft.arcfacesingle.util.scheduler;

import io.reactivex.observers.DisposableObserver;

public abstract class BaseObserver<T> extends DisposableObserver<T> {

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof Exception) {
            onError(ExceptionHandler.handleException(e));
        } else {
            onError(new ExceptionHandler.ResponseThrowable(e, ExceptionHandler.ERROR.UNKNOWN));
        }
    }

    @Override
    public void onComplete() {

    }

    public void onError(ExceptionHandler.ResponseThrowable throwable) {

    }
}
