package com.skillora.payment;

import com.skillora.entities.User;
import com.skillora.services.OrderService;
import com.skillora.services.InvoiceService;
import com.skillora.utils.MyDatabase;
import com.skillora.MoneyFormat;
import com.skillora.Session;
import com.skillora.model.OrderLine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Contrôleur JavaFX pour l'interface de paiement avec conversion TND -> USD.
 */
public class PaymentController {

    @FXML private Label amountLabel; // Montant original (TND)
    @FXML private Label convertedAmountLabel; // Montant converti (USD)
    @FXML private Label orderIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label errorLabel;
    @FXML private VBox statusContainer;
    @FXML private Button payButton;
    @FXML private Button verifyButton;
    @FXML private Button cancelButton;
    @FXML private ProgressIndicator loadingIndicator;

    private final StripeService stripeService = new StripeService();
    private final CurrencyService currencyService = new CurrencyService();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final OrderService orderService = new OrderService();
    private final InvoiceService invoiceService = new InvoiceService();

    private long currentOrderId;
    private double totalAmountTnd;
    private double totalAmountUsd;
    private String currentPaymentId;
    private Runnable onSuccess;

    public void setData(long orderId, double amountTnd, Runnable onSuccess) {
        this.currentOrderId = orderId;
        this.totalAmountTnd = amountTnd;
        this.onSuccess = onSuccess;
        
        amountLabel.setText(String.format("%.2f TND", amountTnd));
        orderIdLabel.setText("#" + orderId);

        // Récupérer le taux de conversion dès l'ouverture
        showLoading("Récupération du taux de change...");
        currencyService.convertTndToUsd(amountTnd)
            .thenAccept(amountUsd -> {
                this.totalAmountUsd = amountUsd;
                Platform.runLater(() -> {
                    convertedAmountLabel.setText(String.format("%.2f USD", amountUsd));
                    hideLoading();
                });
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Erreur conversion : " + ex.getMessage()));
                return null;
            });
    }

    @FXML
    private void onPay() {
        System.out.println("[UI] Clic sur le bouton Payer");
        showLoading("Création de la session Stripe...");
        payButton.setDisable(true);
        cancelButton.setDisable(true);

        stripeService.createPayment(totalAmountUsd, String.valueOf(currentOrderId))
            .thenAccept(response -> {
                if (response != null && response.isSuccess()) {
                    currentPaymentId = response.getSessionId();
                    String url = response.getUrl();
                    
                    try {
                        paymentDAO.savePayment(currentOrderId, currentPaymentId, totalAmountTnd, "EN_ATTENTE");
                    } catch (Exception e) {
                        System.err.println("[DB] Erreur sauvegarde: " + e.getMessage());
                    }

                    if (!"SIMULATION".equals(url)) {
                        CompletableFuture.runAsync(() -> PaymentUtils.openBrowser(url));
                        Platform.runLater(() -> {
                            showStatus("Session Stripe créée. Veuillez payer dans votre navigateur.");
                            verifyButton.setVisible(true);
                            verifyButton.setManaged(true);
                            verifyButton.setDisable(false);
                            payButton.setVisible(false);
                            payButton.setManaged(false);
                        });
                    } else {
                        Platform.runLater(() -> {
                            showStatus("MODE SIMULATION : Cliquez sur 'Vérifier' pour simuler le succès.");
                            verifyButton.setVisible(true);
                            verifyButton.setManaged(true);
                            verifyButton.setDisable(false);
                            payButton.setVisible(false);
                            payButton.setManaged(false);
                        });
                    }
                } else {
                    Platform.runLater(() -> showError("L'API Stripe a renvoyé un échec."));
                }
            })
            .exceptionally(ex -> {
                Platform.runLater(() -> showError("Erreur Stripe : " + ex.getMessage()));
                return null;
            });
    }

    @FXML
    private void onVerify() {
        if (currentPaymentId == null) return;

        showLoading("Vérification Stripe...");
        verifyButton.setDisable(true);

        stripeService.verifyPayment(currentPaymentId)
            .thenAccept(isPaid -> {
                Platform.runLater(() -> {
                    if (isPaid) {
                        handleSuccess();
                    } else {
                        showError("Le paiement n'est pas encore complété.");
                        verifyButton.setDisable(false);
                        loadingIndicator.setVisible(false);
                    }
                });
            })
            .exceptionally(ex -> {
                showError("Erreur de vérification : " + ex.getMessage());
                return null;
            });
    }

    private void handleSuccess() {
        try {
            paymentDAO.updatePaymentStatus(currentPaymentId, "SUCCES");
            orderService.updateStatut(currentOrderId, "PAYEE");
            
            CompletableFuture.runAsync(() -> {
                try {
                    List<OrderLine> lines = orderService.listLines(currentOrderId);
                    // On génère et on ouvre la facture après le succès du paiement
                    invoiceService.generateAndOpenInvoice(currentOrderId, Session.getUser(), lines);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            if (onSuccess != null) onSuccess.run();
            close();
        } catch (Exception e) {
            showError("Erreur post-paiement : " + e.getMessage());
        }
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void showLoading(String msg) {
        Platform.runLater(() -> {
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
            loadingIndicator.setVisible(true);
            statusLabel.setText(msg);
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        });
    }

    private void hideLoading() {
        Platform.runLater(() -> {
            statusContainer.setVisible(false);
            statusContainer.setManaged(false);
        });
    }

    private void showStatus(String msg) {
        Platform.runLater(() -> {
            statusContainer.setVisible(true);
            statusContainer.setManaged(true);
            loadingIndicator.setVisible(false);
            statusLabel.setText(msg);
        });
    }

    private void showError(String msg) {
        Platform.runLater(() -> {
            errorLabel.setText(msg);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
            statusContainer.setVisible(false);
            statusContainer.setManaged(false);
            payButton.setDisable(false);
            cancelButton.setDisable(false);
        });
    }

    private void close() {
        Platform.runLater(() -> {
            Stage stage = (Stage) amountLabel.getScene().getWindow();
            stage.close();
        });
    }
}
