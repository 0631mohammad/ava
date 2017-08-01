/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ir.avageram.com.exoplayer2.source.dash;

import ir.avageram.com.exoplayer2.source.chunk.ChunkSource;
import ir.avageram.com.exoplayer2.source.dash.manifest.DashManifest;
import ir.avageram.com.exoplayer2.trackselection.TrackSelection;
import ir.avageram.com.exoplayer2.upstream.LoaderErrorThrower;

/**
 * An {@link ChunkSource} for DASH streams.
 */
public interface DashChunkSource extends ChunkSource {

  interface Factory {

    DashChunkSource createDashChunkSource(LoaderErrorThrower manifestLoaderErrorThrower,
        DashManifest manifest, int periodIndex, int adaptationSetIndex,
        TrackSelection trackSelection, long elapsedRealtimeOffsetMs);

  }

  void updateManifest(DashManifest newManifest, int periodIndex);

}