package com.pulsepass.notificationservice.domain.gateways.email;

import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailInputDTO;
import com.pulsepass.notificationservice.domain.gateways.email.dtos.SendEmailOutputDTO;

public interface EmailGateway {

    SendEmailOutputDTO sendEmail(SendEmailInputDTO input);
}
