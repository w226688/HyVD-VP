/*
 * Copyright 2016-2017 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.replicator.cmd.parser;

import static com.moilioncircle.redis.replicator.cmd.CommandParsers.toBytes;
import static com.moilioncircle.redis.replicator.cmd.CommandParsers.toInt;
import static com.moilioncircle.redis.replicator.cmd.CommandParsers.toRune;
import static com.moilioncircle.redis.replicator.util.Strings.isEquals;

import com.moilioncircle.redis.replicator.cmd.CommandParser;
import com.moilioncircle.redis.replicator.cmd.impl.DeletionPolicy;
import com.moilioncircle.redis.replicator.cmd.impl.XAckDelCommand;

/**
 * @author Baoyi Chen
 * @since 3.10.0
 */
public class XAckDelParser implements CommandParser<XAckDelCommand> {
    
    @Override
    public XAckDelCommand parse(Object[] command) {
        int idx = 1;
        byte[] key = toBytes(command[idx]);
        idx++;
        byte[] group = toBytes(command[idx]);
        idx++;
        DeletionPolicy policy = null;
        byte[][] ids = null;
        for (; idx < command.length; idx++) {
            String token = toRune(command[idx]);
            if (isEquals(token, "KEEPREF")) {
                policy = DeletionPolicy.KEEPREF;
            } else if (isEquals(token, "DELREF")) {
                policy = DeletionPolicy.DELREF;
            } else if (isEquals(token, "ACKED")) {
                policy = DeletionPolicy.ACKED;
            } else if (isEquals(token, "IDS")) {
                idx++;
                int num = toInt(command[idx]);
                idx++;
                ids = new byte[num][];
                for (int i = 0; i < num; i++, idx++) {
                    ids[i] = toBytes(command[idx]);
                }
            }
        }
        return new XAckDelCommand(key, group, policy, ids);
    }
}
