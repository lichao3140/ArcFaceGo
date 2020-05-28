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

package com.arcsoft.arcfacesingle.util.gson;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;

public class DoubleTypeAdapter extends TypeAdapter<Double> {

    @Override
    public void write(JsonWriter writer, Double value)
            throws IOException {
        if (value == null) {
            writer.nullValue();
            return;
        }
        writer.value(value);
    }

    @Override
    public Double read(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.NULL) {
            reader.nextNull();
            return null;
        }
        if (reader.peek() == JsonToken.STRING) {
            reader.skipValue();
            return -1.0;
        }
        BigDecimal bigDecimal = new BigDecimal(reader.nextString());
        return bigDecimal.doubleValue();
    }
}
