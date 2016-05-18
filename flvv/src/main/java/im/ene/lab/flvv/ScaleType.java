/*
 * Copyright 2016 Nam Nguyen, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.flvv;

/**
 * Created by eneim on 5/16/16.
 */

import java.util.HashMap;
import java.util.Map;

/**
 * Options for scaling the bounds of an image to the bounds of this view.
 */
public enum ScaleType {
  FIT_XY(1),

  FIT_START(2),

  FIT_CENTER(3),

  FIT_END(4),

  CENTER(5),

  CENTER_CROP(6),

  CENTER_INSIDE(7);

  ScaleType(int ni) {
    nativeInt = ni;
  }

  final int nativeInt;

  // cache scale types
  private static final Map<Integer, ScaleType> scaleTypes;

  static {
    scaleTypes = new HashMap<>();
    for (ScaleType screen : ScaleType.values()) {
      scaleTypes.put(screen.nativeInt, screen);
    }
  }

  public static ScaleType lookup(int nativeInt) {
    return scaleTypes.get(nativeInt);
  }
}
