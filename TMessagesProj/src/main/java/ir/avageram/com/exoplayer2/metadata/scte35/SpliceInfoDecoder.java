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
package ir.avageram.com.exoplayer2.metadata.scte35;

import android.text.TextUtils;
import ir.avageram.com.exoplayer2.metadata.Metadata;
import ir.avageram.com.exoplayer2.metadata.MetadataDecoder;
import ir.avageram.com.exoplayer2.metadata.MetadataDecoderException;
import ir.avageram.com.exoplayer2.util.MimeTypes;
import ir.avageram.com.exoplayer2.util.ParsableBitArray;
import ir.avageram.com.exoplayer2.util.ParsableByteArray;

/**
 * Decodes splice info sections and produces splice commands.
 */
public final class SpliceInfoDecoder implements MetadataDecoder {

  private static final int TYPE_SPLICE_NULL = 0x00;
  private static final int TYPE_SPLICE_SCHEDULE = 0x04;
  private static final int TYPE_SPLICE_INSERT = 0x05;
  private static final int TYPE_TIME_SIGNAL = 0x06;
  private static final int TYPE_PRIVATE_COMMAND = 0xFF;

  private final ParsableByteArray sectionData;
  private final ParsableBitArray sectionHeader;

  public SpliceInfoDecoder() {
    sectionData = new ParsableByteArray();
    sectionHeader = new ParsableBitArray();
  }

  @Override
  public boolean canDecode(String mimeType) {
    return TextUtils.equals(mimeType, MimeTypes.APPLICATION_SCTE35);
  }

  @Override
  public Metadata decode(byte[] data, int size) throws MetadataDecoderException {
    sectionData.reset(data, size);
    sectionHeader.reset(data, size);
    // table_id(8), section_syntax_indicator(1), private_indicator(1), reserved(2),
    // section_length(12), protocol_version(8), encrypted_packet(1), encryption_algorithm(6).
    sectionHeader.skipBits(39);
    long ptsAdjustment = sectionHeader.readBits(1);
    ptsAdjustment = (ptsAdjustment << 32) | sectionHeader.readBits(32);
    // cw_index(8), tier(12).
    sectionHeader.skipBits(20);
    int spliceCommandLength = sectionHeader.readBits(12);
    int spliceCommandType = sectionHeader.readBits(8);
    SpliceCommand command = null;
    // Go to the start of the command by skipping all fields up to command_type.
    sectionData.skipBytes(14);
    switch (spliceCommandType) {
      case TYPE_SPLICE_NULL:
        command = new SpliceNullCommand();
        break;
      case TYPE_SPLICE_SCHEDULE:
        command = SpliceScheduleCommand.parseFromSection(sectionData);
        break;
      case TYPE_SPLICE_INSERT:
        command = SpliceInsertCommand.parseFromSection(sectionData, ptsAdjustment);
        break;
      case TYPE_TIME_SIGNAL:
        command = TimeSignalCommand.parseFromSection(sectionData, ptsAdjustment);
        break;
      case TYPE_PRIVATE_COMMAND:
        command = PrivateCommand.parseFromSection(sectionData, spliceCommandLength, ptsAdjustment);
        break;
    }
    return command == null ? new Metadata() : new Metadata(command);
  }

}