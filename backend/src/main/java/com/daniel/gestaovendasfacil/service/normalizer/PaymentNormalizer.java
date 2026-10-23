package com.daniel.gestaovendasfacil.service.normalizer;

import com.daniel.gestaovendasfacil.model.BandeiraCartao;
import com.daniel.gestaovendasfacil.model.FormaPagamento;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PaymentNormalizer {

    private static final Pattern PARCELAS_PATTERN = Pattern.compile("(\\d+)\\s*[xX]");

    public PaymentResult normalize(String rawPagamento, String rawCartao, String rawVezes) {
        String cleanPag = clean(rawPagamento);
        String cleanCard = clean(rawCartao);
        String cleanVez = clean(rawVezes);

        if (cleanPag.isEmpty()) {
            return new PaymentResult(FormaPagamento.NAO_INFORMADO, null, 1, true, "Forma de pagamento não informada");
        }

        FormaPagamento forma = null;
        BandeiraCartao bandeira = parseBandeira(cleanCard);
        Integer parcelas = parseParcelas(cleanVez);

        // Detectar parcelas embutidas em como pagou (ex: "credito 3x visa")
        Matcher mParcelas = PARCELAS_PATTERN.matcher(cleanPag);
        if (mParcelas.find()) {
            try {
                parcelas = Integer.parseInt(mParcelas.group(1));
            } catch (Exception ignored) {
            }
        }

        // Detectar bandeira embutida em como pagou
        if (bandeira == null) {
            bandeira = parseBandeira(cleanPag);
        }

        if (cleanPag.contains("pix")) {
            forma = FormaPagamento.PIX;
        } else if (cleanPag.contains("dinheiro")) {
            forma = FormaPagamento.DINHEIRO;
        } else if (cleanPag.contains("debito")) {
            forma = FormaPagamento.DEBITO;
        } else if (cleanPag.contains("credito")) {
            forma = FormaPagamento.CREDITO;
        } else if (cleanPag.contains("maquin")) { // maquinha, maquininha
            forma = FormaPagamento.MAQUININHA;
        } else if (cleanPag.contains("fiado")) {
            forma = FormaPagamento.FIADO;
        } else if (cleanPag.contains("depois paga") || cleanPag.contains("pagar depois")) {
            forma = FormaPagamento.PAGAR_DEPOIS;
        }

        if (forma == null) {
            return new PaymentResult(FormaPagamento.NAO_INFORMADO, bandeira, parcelas != null ? parcelas : 1, true, "Forma de pagamento não reconhecida (" + rawPagamento + ")");
        }

        if (forma == FormaPagamento.CREDITO) {
            if (parcelas == null || parcelas < 1) {
                parcelas = 1;
            }
        }

        if ((forma == FormaPagamento.DEBITO || forma == FormaPagamento.CREDITO || forma == FormaPagamento.MAQUININHA)) {
            if (bandeira == null) {
                bandeira = BandeiraCartao.NAO_INFORMADO;
            }
        }

        return new PaymentResult(forma, bandeira, parcelas != null ? parcelas : 1, false, null);
    }

    private BandeiraCartao parseBandeira(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        if (text.contains("visa")) {
            return BandeiraCartao.VISA;
        } else if (text.contains("master")) {
            return BandeiraCartao.MASTER;
        } else if (text.contains("elo")) {
            return BandeiraCartao.ELO;
        } else if (text.contains("outra")) {
            return BandeiraCartao.OUTRA;
        }
        return null;
    }

    private Integer parseParcelas(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            String digits = text.replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                return Integer.parseInt(digits);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String clean(String text) {
        if (text == null) {
            return "";
        }
        String nfd = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}

