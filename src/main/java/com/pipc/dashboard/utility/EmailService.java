package com.pipc.dashboard.utility;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

	public void sendEmail(String toEmail, String subject, String body) {

		Email from = new Email("applizrsol@gmail.com"); // your sender email
		Email to = new Email(toEmail);

		Content content = new Content("text/plain", body);
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid("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");

		Request request = new Request();
		try {
			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");
			request.setBody(mail.build());

			Response response = sg.api(request);

			System.out.println("Email Status Code: " + response.getStatusCode());
			System.out.println("Email Response Body: " + response.getBody());
			System.out.println("Email Response Headers: " + response.getHeaders());

		} catch (IOException ex) {
			throw new RuntimeException("Email sending failed: " + ex.getMessage(), ex);
		}
	}
}