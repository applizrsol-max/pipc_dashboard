package com.pipc.dashboard.utility;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

public class PdfUtil {

	public static PDType0Font loadFont(PDDocument pdf) {
		try {
			InputStream is = PdfUtil.class.getResourceAsStream("/fonts/MANGAL.TTF");

			if (is == null) {
				System.out.println("❌ Font file NOT found in resources/fonts/");
				return null;
			}

			return PDType0Font.load(pdf, is, true);

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void drawText(PDPageContentStream cs, String text, float x, float y, PDType0Font font, int fontSize,
			boolean bold) throws IOException {

		cs.beginText();
		cs.setFont(font, fontSize);
		cs.newLineAtOffset(x, y);
		cs.showText(text);
		cs.endText();
	}
}
