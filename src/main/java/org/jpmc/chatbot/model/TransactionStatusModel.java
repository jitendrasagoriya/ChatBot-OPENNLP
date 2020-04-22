package org.jpmc.chatbot.model;

import org.apache.commons.lang3.StringUtils;


/**
 * Custom chat bot or chat agent for automated chat replies for FAQs. It uses
 * different features of Apache OpenNLP for understanding what user is asking
 * for. NLP is natural language processing.
 *
 * @author Jitendra Sagoriya
 *
 */

public class TransactionStatusModel {

    private String bankId;
    private String branchId;
    private String accountId;
    private String txnDate;

    private static TransactionStatusModel SINGLE_INSTANCE = null;



    private TransactionStatusModel() {
        this.bankId = null;
        this.branchId = null;
        this.accountId = null;
        this.txnDate = null;
    }

    public static TransactionStatusModel getInstance() {
        if (SINGLE_INSTANCE == null) {
            synchronized(TransactionStatusModel.class) {
                SINGLE_INSTANCE = new TransactionStatusModel();
            }
        }
        return SINGLE_INSTANCE;
    }

    public boolean checkedAllDataDone() {
        if( StringUtils.isBlank(this.bankId) )
            return false;
        if( StringUtils.isBlank(this.branchId) )
            return false;
        if( StringUtils.isBlank(this.accountId) )
            return false;
        if( StringUtils.isBlank(this.txnDate) )
            return false;
        return true;
    }


    public String getBankId() {
        return bankId;
    }

    public void setBankId(String bankId) {
        this.bankId = bankId;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getTxnDate() {
        return txnDate;
    }

    public void setTxnDate(String txnDate) {
        this.txnDate = txnDate;
    }
}
