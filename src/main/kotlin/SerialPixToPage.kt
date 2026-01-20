import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

fun main() {
    // macOS native görünüm için
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val images = selectImages() ?: return

    //val outputPdf = "/Users/Shared/output.pdf"
    val outputPdf= selectSaveLocation() ?: return
    createSeriesPdfFromImages(images, outputPdf)

    println("PDF oluşturuldu: $outputPdf")
}
fun createSeriesPdfFromImages(images: List<File>, outputPdf: String) {
    val document = PDDocument()

    val pageWidth = PDRectangle.A4.width
    val pageHeight = PDRectangle.A4.height

    val margin = 20f
    val columnSpacing = 20f
    val imageSpacing = 15f

    val columnWidth = (pageWidth - (margin * 2) - columnSpacing) / 2f

    var page = PDPage(PDRectangle.A4)
    document.addPage(page)
    var contentStream = PDPageContentStream(document, page)

    val leftColumnX = margin
    val rightColumnX = margin + columnWidth + columnSpacing

    var currentX = leftColumnX
    var currentY = pageHeight - margin

    var isLeftColumn = true

    var imageIndex = 1   // <<--- RESİM SAYACI

    for (imgFile in images) {

        val pdImage = PDImageXObject.createFromFile(imgFile.absolutePath, document)

        val scale = columnWidth / pdImage.width
        val imgWidth = pdImage.width * scale
        val imgHeight = pdImage.height * scale

        if (currentY - imgHeight < margin) {
            if (isLeftColumn) {
                currentX = rightColumnX
                currentY = pageHeight - margin
                isLeftColumn = false
            } else {
                contentStream.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)

                currentX = leftColumnX
                currentY = pageHeight - margin
                isLeftColumn = true
            }
        }

        // ----------------------------------------------------------
        //        RESİM NUMARASINI ÜSTE YAZ (textHeight = 12pt)
        // ----------------------------------------------------------
        val text = "$imageIndex"

        contentStream.beginText()
        contentStream.setFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 12f)
        contentStream.newLineAtOffset(currentX, currentY - 12f)  // biraz yukarı
        contentStream.showText(text)
        contentStream.endText()

        val adjustedY = currentY - 20f  // yazı ile resim arasında boşluk

        // ----------------------------------------------------------
        //                     RESMİ ÇİZ
        // ----------------------------------------------------------
        contentStream.drawImage(pdImage, currentX, adjustedY - imgHeight, imgWidth, imgHeight)

        currentY = adjustedY - imgHeight - imageSpacing

        imageIndex++ // <<-- BİR SONRAKİ RESİM NUMARASI
    }

    contentStream.close()
    document.save(outputPdf)
    document.close()
}
fun createSeriesPdfFromImagesNumarasiz(images: List<File>, outputPdf: String) {
    val document = PDDocument()

    val pageWidth = PDRectangle.A4.width
    val pageHeight = PDRectangle.A4.height

    val margin = 20f
    val columnSpacing = 20f
    val imageSpacing = 15f

    val columnWidth = (pageWidth - (margin * 2) - columnSpacing) / 2f

    var page = PDPage(PDRectangle.A4)
    document.addPage(page)
    var contentStream = PDPageContentStream(document, page)

    // Sol sütun ve sağ sütun başlangıç X pozisyonları
    val leftColumnX = margin
    val rightColumnX = margin + columnWidth + columnSpacing

    var currentX = leftColumnX
    var currentY = pageHeight - margin

    var isLeftColumn = true

    for (imgFile in images) {

        val pdImage = PDImageXObject.createFromFile(imgFile.absolutePath, document)

        // Resmi sütuna sığdırmak için ölçekle
        val scale = columnWidth / pdImage.width
        val imgWidth = pdImage.width * scale
        val imgHeight = pdImage.height * scale

        // Eğer resim sığmıyorsa aynı sütunda → diğer sütuna geç
        if (currentY - imgHeight < margin) {
            if (isLeftColumn) {
                // Sağ sütuna geç
                currentX = rightColumnX
                currentY = pageHeight - margin
                isLeftColumn = false
            } else {
                // Yeni sayfa aç, tekrar sol sütundan başla
                contentStream.close()
                page = PDPage(PDRectangle.A4)
                document.addPage(page)
                contentStream = PDPageContentStream(document, page)

                currentX = leftColumnX
                currentY = pageHeight - margin
                isLeftColumn = true
            }
        }

        // Resmi çiz
        contentStream.drawImage(pdImage, currentX, currentY - imgHeight, imgWidth, imgHeight)

        // Bir satır boşluk
        currentY -= imgHeight + imageSpacing
    }

    contentStream.close()
    document.save(outputPdf)
    document.close()
}
