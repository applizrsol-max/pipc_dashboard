package com.pipc.dashboard.utility;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

@Service
public class EmailService {

	@Value("${SENDGRID_API_KEY}")
	private String emailSec;

	public void sendEmail(String toEmail, String subject, String body) {

		Email from = new Email("applizrsol@gmail.com"); // your sender email
		Email to = new Email(toEmail);

		Content content = new Content("text/plain", body);
		Mail mail = new Mail(from, subject, to, content);

		SendGrid sg = new SendGrid(emailSec);

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