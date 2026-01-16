package com.coderdream.util.gemini.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetySetting {

    private HarmCategory category;
    private HarmBlockThreshold threshold;

    public static SafetySetting of(HarmCategory category, HarmBlockThreshold threshold) {
        return new SafetySetting(category, threshold);
    }

    public enum HarmBlockThreshold {
        HARM_BLOCK_THRESHOLD_UNSPECIFIED,
        BLOCK_LOW_AND_ABOVE,
        BLOCK_MEDIUM_AND_ABOVE,
        BLOCK_ONLY_HIGH,
        BLOCK_NONE
    }
}
