package org.jpmc.chatbot.helper;

import org.apache.commons.lang3.StringUtils;
import org.jpmc.chatbot.config.MissingTranscationConfig;
import org.jpmc.chatbot.model.TransactionStatusModel;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom chat bot or chat agent for automated chat replies for FAQs. It uses
 * different features of Apache OpenNLP for understanding what user is asking
 * for. NLP is natural language processing.
 *
 * @author Jitendra Sagoriya
 *
 */

public class MissingTranscationHelper {



    private static Map<String, String> dataCategoryMap = new HashMap<>();
    static {
        dataCategoryMap.put("bank","missing-transaction");
        dataCategoryMap.put("branch","bank-continue");
        dataCategoryMap.put("account","branch-continue");
        dataCategoryMap.put("date","account-continue");
    }

    public static void setTxnStarted() {
        MissingTranscationConfig.getInstance().setStarted(Boolean.TRUE);
    }

    public static void setTxnEnd() {
        MissingTranscationConfig.getInstance().setFinished(Boolean.TRUE);
    }

    public static String getCategory(String key) {
        return dataCategoryMap.get(key);
    }

    public static void checkResponse(String response) {
        if(StringUtils.equals("bank-continue",response)
            || StringUtils.equals("branch-continue",response)
            || StringUtils.equals("account-continue",response)
            || StringUtils.equals("date-of-transaction",response) ) {
            setTxnStarted();
        }
    }


    public static void setValues(String response, String value) {
        if(StringUtils.equals("bank-continue",response)) {
            TransactionStatusModel.getInstance().setBankId(value);
            MissingTranscationConfig.getInstance().setInputParamMapValues("bank");
        }

        if(StringUtils.equals("branch-continue",response)) {
            TransactionStatusModel.getInstance().setBranchId(value);
            MissingTranscationConfig.getInstance().setInputParamMapValues("branch");
        }

        if(StringUtils.equals("account-continue",response)) {
            TransactionStatusModel.getInstance().setAccountId(value);
            MissingTranscationConfig.getInstance().setInputParamMapValues("account");
        }

        if(StringUtils.equals("date-of-transaction",response)) {
            TransactionStatusModel.getInstance().setTxnDate(value);
            MissingTranscationConfig.getInstance().setInputParamMapValues("date");
        }
    }



}
