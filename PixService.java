import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.Charset;

@Service
public class PixService {

    @Value("${pix.beneficiario}")
    private String BENEFICIARIO;

    @Value("${pix.cidade}")
    private String CIDADE;

    @Value("${pix.chave}")
    private String CHAVE_PIX;

    private static final String FORMAT_INDICATOR = "000201";
    private static final String ACCOUNT_INFORMATION = "26";
    private static final String MERCHANT_CATEGORY_CODE = "52040000";
    private static final String TRANSACTION_CURRENCY = "5303986540";
    private static final String COUNTRY_CODE = "5802BR";
    private static final String CODE_MERCHANT_NAME = "5908";
    private static final String CODE_MERCHANT_CITY = "600";
    private static final String ADD_DATA_FIELD_TEMPLATE = "62";

    private static String CRC16CCITT(String payload) {
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)

        byte[] bytes = payload.getBytes(Charset.defaultCharset());

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }

        crc &= 0xffff;
        return Integer.toHexString(crc);
    }

    public String generatePixQrCode(String valorTotal, long identificadorTransacao) {

        String MERCHANT_ACCOUNT_INFORMATION_STRING = "0014BR.GOV.BCB.PIX01" + CHAVE_PIX.length() +
            CHAVE_PIX;

        String descricao = String.valueOf(identificadorTransacao);
        String txid = "05" + String.format("%02d", descricao.length()) + descricao;

        String payload = FORMAT_INDICATOR +
                        ACCOUNT_INFORMATION +
                        MERCHANT_ACCOUNT_INFORMATION_STRING.length() +
                        MERCHANT_ACCOUNT_INFORMATION_STRING +
                        MERCHANT_CATEGORY_CODE +
                        TRANSACTION_CURRENCY +
                        valorTotal.length() +
                        valorTotal +
                        COUNTRY_CODE +
                        CODE_MERCHANT_NAME +
                        BENEFICIARIO +
                        CODE_MERCHANT_CITY +
                        CIDADE.length() +
                        CIDADE +
                        ADD_DATA_FIELD_TEMPLATE +
                        txid.length() +
                        txid + "6304";

        String CRC = CRC16CCITT(payload);
        return payload + StringUtils.upperCase(CRC);
    }

}
