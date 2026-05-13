package Services;

import Entities.User;
import Entities.Coupon;
import com.skillora.model.OrderLine;
import utils.PdfGenerator;

import java.io.File;
import java.util.List;

/**
 * Service gérant la logique métier des factures.
 */
public class InvoiceService {

    private final PdfGenerator pdfGenerator = new PdfGenerator();
    private final CouponService couponService = new CouponService();

    /**
     * Génère la facture d'une commande sans l'ouvrir automatiquement.
     *
     * @param idCommande L'identifiant de la commande.
     * @param user L'utilisateur client.
     * @param lines Les lignes de la commande.
     */
    public void generateInvoiceOnly(long idCommande, User user, List<OrderLine> lines) {
        try {
            // Récupérer le coupon utilisé pour cette commande
            Coupon usedCoupon = couponService.getUsedCoupon(idCommande);

            // Génération du fichier PDF
            File invoice = pdfGenerator.generateInvoice(idCommande, user, lines, usedCoupon);
            
            System.out.println("Facture générée avec succès (sans ouverture) : " + invoice.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Génère et ouvre la facture d'une commande.
     *
     * @param idCommande L'identifiant de la commande.
     * @param user L'utilisateur client.
     * @param lines Les lignes de la commande.
     */
    public void generateAndOpenInvoice(long idCommande, User user, List<OrderLine> lines) {
        try {
            // Récupérer le coupon utilisé pour cette commande
            Coupon usedCoupon = couponService.getUsedCoupon(idCommande);

            // Génération du fichier PDF
            File invoice = pdfGenerator.generateInvoice(idCommande, user, lines, usedCoupon);
            
            // Ouverture automatique du PDF
            pdfGenerator.openPdf(invoice);
            
            System.out.println("Facture générée avec succès : " + invoice.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Erreur lors de la génération de la facture : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
