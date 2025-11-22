package com.unfbx.chatgpt.entity.assistant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 描述：
 *
 * @author https://www.unfbx.com
 * @since 2024-05-07
 */
@Data
@Builder
public class CodeInterpreter {

    @JsonProperty("file_ids")
    private List<String> fileIds;
}
