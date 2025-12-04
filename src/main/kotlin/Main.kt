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
    createPdfFromImages(images, outputPdf)

    println("PDF oluşturuldu: $outputPdf")
}

fun selectImages(): List<File>? {
    val chooser = JFileChooser()
    chooser.dialogTitle = "Resimleri Seç"
    chooser.isMultiSelectionEnabled = true
    chooser.isAcceptAllFileFilterUsed = false

    chooser.fileFilter = javax.swing.filechooser.FileNameExtensionFilter(
        "Image Files (png, jpg)",
        "png", "jpg", "jpeg"
    )

    val result = chooser.showOpenDialog(null)

    if (result == JFileChooser.APPROVE_OPTION) {
        return chooser.selectedFiles.toList()
    }

    println("Hiç dosya seçilmedi.")
    return null
}

fun createPdfFromImages(images: List<File>, outputPdf: String) {
    val document = PDDocument()

    for (imgFile in images) {
        val page = PDPage(PDRectangle.A4)
        document.addPage(page)

        val pdImage = PDImageXObject.createFromFile(imgFile.absolutePath, document)

        val contentStream = PDPageContentStream(document, page)
        val margin = 20f
        val pageWidth = page.mediaBox.width

        val scale = (pageWidth - margin * 2) / pdImage.width

        val imgWidth = pdImage.width * scale
        val imgHeight = pdImage.height * scale

        val yPosition = page.mediaBox.height - imgHeight - margin

        contentStream.drawImage(
            pdImage,
            margin,
            yPosition,
            imgWidth,
            imgHeight
        )

        contentStream.close()
    }

    document.save(outputPdf)
    document.close()
}

fun selectSaveLocation(): String? {
    val chooser = JFileChooser()
    chooser.dialogTitle = "PDF Kaydedilecek Yeri Seç"
    chooser.dialogType = JFileChooser.SAVE_DIALOG
    chooser.selectedFile = File("output.pdf")  // varsayılan isim

    chooser.fileFilter = FileNameExtensionFilter("PDF File", "pdf")

    val result = chooser.showSaveDialog(null)

    if (result == JFileChooser.APPROVE_OPTION) {
        var file = chooser.selectedFile

        // Uzantı yazılmadıysa otomatik ekle
        if (!file.absolutePath.lowercase().endsWith(".pdf")) {
            file = File(file.absolutePath + ".pdf")
        }

        return file.absolutePath
    }

    println("Kayıt iptal edildi.")
    return null
}

