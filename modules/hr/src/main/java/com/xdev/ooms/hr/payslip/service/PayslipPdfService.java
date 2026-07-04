package com.xdev.ooms.hr.payslip.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.xdev.ooms.sharedkernel.ports.HrPayRollReadPort;
import com.xdev.ooms.sharedkernel.ports.HrPayRollSnapshot;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PayslipPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private final HrPayRollReadPort hrPayRollReadPort;

    public PayslipPdfService(HrPayRollReadPort hrPayRollReadPort) {
        this.hrPayRollReadPort = hrPayRollReadPort;
    }

    public byte[] generatePayslipPdf(UUID payslipId) {
        HrPayRollSnapshot snapshot = hrPayRollReadPort.findPayRollForPdf(payslipId)
                .orElseThrow(() -> new EntityNotFoundException("Payslip not found: " + payslipId));
        return buildPdf(snapshot);
    }

    private byte[] buildPdf(HrPayRollSnapshot snapshot) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
            Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            Paragraph title = new Paragraph("Bulletin de paie / Payslip", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            document.add(line("Employee", employeeName(snapshot), labelFont, valueFont));
            document.add(line("CIN", nullSafe(snapshot.employeeCin()), labelFont, valueFont));
            document.add(line("CNSS", nullSafe(snapshot.employeeCnssMatricule()), labelFont, valueFont));
            document.add(line(
                    "Period",
                    formatPeriod(snapshot.periodStart(), snapshot.periodEnd()),
                    labelFont,
                    valueFont
            ));
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            addAmountRow(table, "Base salary", snapshot.baseSalary(), labelFont, valueFont);
            addAmountRow(table, "Bonuses", snapshot.bonuses(), labelFont, valueFont);
            addAmountRow(table, "Gross salary", snapshot.grossSalary(), labelFont, valueFont);
            addAmountRow(table, "CNSS employee (9.68%)", snapshot.cnssEmployee(), labelFont, valueFont);
            addAmountRow(table, "CNSS employer (17.07%)", snapshot.cnssEmployer(), labelFont, valueFont);
            addAmountRow(table, "CSS (0.5%)", snapshot.css(), labelFont, valueFont);
            addAmountRow(table, "IRPP", snapshot.irpp(), labelFont, valueFont);
            addAmountRow(table, "Net salary", snapshot.netSalary(), labelFont, valueFont);
            document.add(table);

            document.add(Chunk.NEWLINE);
            document.add(line(
                    "Payment status",
                    Boolean.TRUE.equals(snapshot.paid()) ? "Paid" : "Unpaid",
                    labelFont,
                    valueFont
            ));
            if (snapshot.paymentDate() != null) {
                document.add(line("Payment date", DATE_FMT.format(snapshot.paymentDate()), labelFont, valueFont));
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Failed to generate payslip PDF", e);
        }
    }

    private Paragraph line(String label, String value, Font labelFont, Font valueFont) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + ": ", labelFont));
        paragraph.add(new Chunk(value, valueFont));
        return paragraph;
    }

    private void addAmountRow(PdfPTable table, String label, Double amount, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        PdfPCell valueCell = new PdfPCell(new Phrase(formatAmount(amount), valueFont));
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private String employeeName(HrPayRollSnapshot snapshot) {
        String first = nullSafe(snapshot.employeeFirstName());
        String last = nullSafe(snapshot.employeeLastName());
        return (first + " " + last).trim();
    }

    private String formatPeriod(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null && end == null) {
            return "-";
        }
        if (start != null && end != null) {
            return DATE_FMT.format(start) + " → " + DATE_FMT.format(end);
        }
        return start != null ? DATE_FMT.format(start) : DATE_FMT.format(end);
    }

    private String formatAmount(Double value) {
        if (value == null) {
            return "0.000";
        }
        return String.format("%.3f TND", value);
    }

    private String nullSafe(String value) {
        return value != null ? value : "-";
    }
}
