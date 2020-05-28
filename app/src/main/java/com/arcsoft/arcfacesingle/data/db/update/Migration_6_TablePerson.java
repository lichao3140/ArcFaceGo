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

package com.arcsoft.arcfacesingle.data.db.update;

import com.arcsoft.arcfacesingle.data.db.DBManager;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson_Table;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

@Migration(version = 6, database = DBManager.class)
public class Migration_6_TablePerson extends AlterTableMigration<TablePerson> {

    public Migration_6_TablePerson(Class<TablePerson> personClass) {
        super(personClass);
    }

    @Override
    public void onPreMigrate() {
        addColumn(SQLiteType.TEXT, TablePerson_Table.icCardNo.getNameAlias().name());
        addColumn(SQLiteType.INTEGER, TablePerson_Table.personInfoType.getNameAlias().name());
    }
}
