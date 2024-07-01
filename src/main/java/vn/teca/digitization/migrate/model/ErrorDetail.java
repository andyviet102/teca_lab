package vn.teca.digitization.migrate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ErrorDetail {
    private Date timestamp;

    private String message;

    private Object detail;

    private String path;
}
