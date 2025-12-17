package com.ssafy.fitmarket_be.payment.dto;

public record TossPaymentResponse(
    String paymentKey,
    String orderId,
    String status,
    String approvedAt,
    Long totalAmount,
    String method

    // 응답 받아오는 정보들
    // 참고 API 명세서 -> https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4
    /*
        key: mId
        key: lastTransactionKey
        key: paymentKey
        key: orderId
        key: orderName
        key: taxExemptionAmount
        key: status
        key: requestedAt
        key: approvedAt
        key: useEscrow
        key: cultureExpense
        key: card
        key: virtualAccount
        key: transfer
        key: mobilePhone
        key: giftCertificate
        key: cashReceipt
        key: cashReceipts
        key: discount
        key: cancels
        key: secret
        key: type
        key: easyPay
        key: country
        key: failure
        key: isPartialCancelable
        key: receipt
        key: checkout
        key: currency
        key: totalAmount
        key: balanceAmount
        key: suppliedAmount
        key: vat
        key: taxFreeAmount
        key: method
        key: version
        key: metadata
     */
) {

}
