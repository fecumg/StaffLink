package fpt.edu.fileservice.exchangeDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Truong Duc Duong
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeAttachment {
    private String taskId;
    private String filename;
}
