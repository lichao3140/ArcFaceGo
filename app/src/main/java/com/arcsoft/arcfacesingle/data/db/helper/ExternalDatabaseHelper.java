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

package com.arcsoft.arcfacesingle.data.db.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.arcsoft.arcfacesingle.data.db.table.TableArcFaceVersion;
import com.arcsoft.arcfacesingle.data.db.table.TablePerson;
import com.arcsoft.arcfacesingle.data.db.table.TablePersonFace;
import com.raizlabs.android.dbflow.config.FlowManager;

import java.util.ArrayList;
import java.util.List;

public class ExternalDatabaseHelper {

    public static SQLiteDatabase getDatabase(String dbPath) {
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    public static long getTablePersonCount(SQLiteDatabase database) {
        try {
            String sql = "select count(*) from " + FlowManager.getTableName(TablePerson.class);
            Cursor cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            long count = cursor.getLong(0);
            cursor.close();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getTablePersonFaceCount(SQLiteDatabase database) {
        try {
            String sql = "select count(*) from " + FlowManager.getTableName(TablePersonFace.class);
            Cursor cursor = database.rawQuery(sql, null);
            cursor.moveToFirst();
            long count = cursor.getLong(0);
            cursor.close();
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static List<TablePerson> getPersonListFromDb(SQLiteDatabase database) {
        List<TablePerson> personList = new ArrayList<>();
        try {
            String sql = "select personSerial, personName, addTime, updateTime, doorAuthorityDetail," +
                    " authMorningStartTime, authMorningEndTime, authNoonStartTime, authNoonEndTime, authNightStartTime," +
                    " authNightEndTime, personInfoNo, icCardNo, personInfoType from " +
                    FlowManager.getTableName(TablePerson.class);
            Cursor cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                TablePerson tablePerson = new TablePerson();
                tablePerson.personSerial = cursor.getString(0);
                String personName = cursor.getString(1);
                if (!TextUtils.isEmpty(personName)) {
                    personName = personName.replaceAll("'", "");
                }
                tablePerson.personName = personName;
                tablePerson.addTime = cursor.getLong(2);
                tablePerson.updateTime = cursor.getLong(3);
                tablePerson.doorAuthorityDetail = cursor.getString(4);
                tablePerson.authMorningStartTime = cursor.getString(5);
                tablePerson.authMorningEndTime = cursor.getString(6);
                tablePerson.authNoonStartTime = cursor.getString(7);
                tablePerson.authNoonEndTime = cursor.getString(8);
                tablePerson.authNightStartTime = cursor.getString(9);
                tablePerson.authNightEndTime = cursor.getString(10);
                String personInfoNo = cursor.getString(11);
                if (!TextUtils.isEmpty(personInfoNo)) {
                    personInfoNo = personInfoNo.replaceAll("'", "");
                }
                tablePerson.personInfoNo = personInfoNo;
                tablePerson.icCardNo = cursor.getString(12);
                tablePerson.personInfoType = cursor.getInt(13);
                personList.add(tablePerson);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return personList;
    }

    public static List<TablePersonFace> getPersonFaceList(SQLiteDatabase database) {
        List<TablePersonFace> personFaces = new ArrayList<>();
        try {
            String sql = "select personSerial, imagePath, imageMD5, feature, featureVersion, addTime, updateTime," +
                    " faceInfo from " + FlowManager.getTableName(TablePersonFace.class);
            Cursor cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                TablePersonFace face = new TablePersonFace();
                face.personSerial = cursor.getString(0);
                face.imagePath = cursor.getString(1);
                face.imageMD5 = cursor.getString(2);
                face.feature = cursor.getBlob(3);
                face.featureVersion = cursor.getString(4);
                face.addTime = cursor.getLong(5);
                face.updateTime = cursor.getLong(6);
                face.faceInfo = cursor.getString(7);
                personFaces.add(face);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return personFaces;
    }

    public static List<TableArcFaceVersion> getArcFaceVersionList(SQLiteDatabase database) {
        List<TableArcFaceVersion> arcFaceVersions = new ArrayList<>();
        try {
            String sql = "select id, version from " + FlowManager.getTableName(TableArcFaceVersion.class) +
                    " order by id desc";
            Cursor cursor = database.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                TableArcFaceVersion version = new TableArcFaceVersion();
                version.id = cursor.getInt(0);
                version.version = cursor.getString(1);
                arcFaceVersions.add(version);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arcFaceVersions;
    }
}
