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

package com.arcsoft.arcfacesingle.data.db;

import com.arcsoft.arcfacesingle.app.Constants;
import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = DBManager.NAME, version = DBManager.VERSION)
public class DBManager {

    public static final String NAME = Constants.DATA_BASE_NAME;

    public static final int VERSION = 10;
}
