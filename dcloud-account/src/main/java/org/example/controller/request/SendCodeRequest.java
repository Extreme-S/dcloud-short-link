package org.example.controller.request;

import lombok.Data;


@Data
public class SendCodeRequest {

    private String kaptcha;

    private String to;
}
