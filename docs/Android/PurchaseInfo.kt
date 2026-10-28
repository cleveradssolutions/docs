package com.cleveradssolutions.sdk

import androidx.annotation.IntRange
import com.cleveradssolutions.internal.getStringOrNull
import com.cleveradssolutions.internal.services.Session
import com.cleveradssolutions.mediation.debug
import com.cleveradssolutions.sdk.PurchaseInfo.Companion.PROVIDER_AMAZON_APPSTORE
import com.cleveradssolutions.sdk.PurchaseInfo.Companion.PROVIDER_GOOGLE_BILLING
import com.cleveradssolutions.sdk.PurchaseInfo.Companion.PROVIDER_XSOLLA
import org.json.JSONObject

/**
 * Information about an in-app purchase.
 *
 * The total revenue generated will be the: [quantity] * [price] * `currency_rate`
 *
 * Example for Google Play Billing:
 * ```
 * CAS.reportPurchase(
 *  new PurchaseInfo(
 *      purchase.getProducts().get(0),
 *      price,
 *      currency,
 *      purchase.getQuantity()
 *  )
 *  // Recommended add info to validate purchase
 *  .setValidation(
 *      purchase.getOriginalJson(),
 *      purchase.getSignature()
 *  )
 *  // If productType == BillingClient.ProductType.SUBS
 *  .setSubsValidation(
 *      purchase.getOriginalJson(),
 *      purchase.getSignature(),
 *      purchase.getPurchaseToken(),
 *      purchase.getPurchaseTime()
 *  )
 * )
 * ```
 */
class PurchaseInfo(
    /**
     * Product identifier from the IAP provider.
     */
    val productId: String,
    /**
     * Price of one purchased item in the selected currency.
     */
    val price: Double,
    /**
     * ISO 4217 currency code, for example USD or EUR.
     */
    val currency: String,
    /**
     * Number of items being purchased.
     */
    @field:IntRange(from = 1)
    val quantity: Int,
) {

    /**
     * Original json data about purchase used for validation.
     */
    var json: String? = null
        private set

    /**
     * Google Play signature of the purchase data or Amazon receipt identifier.
     */
    var signature: String? = null
        private set

    /**
     * Subscription purchase time in milliseconds since Unix epoch. Zero for regular purchases.
     */
    var subscriptionTime: Long = -1
        private set

    /**
     * Google Play subscription token.
     */
    var subscriptionToken: String? = null
        private set

    /**
     * User ID for the Amazon IAP system.
     */
    var userId: String? = null
        private set

    /**
     * Provider that processed the purchase.
     * [PROVIDER_GOOGLE_BILLING], [PROVIDER_AMAZON_APPSTORE], [PROVIDER_XSOLLA]
     */
    var provider: String = PROVIDER_GOOGLE_BILLING

    init {
        assert(productId.isNotEmpty()) {
            "Product ID cannot be empty"
        }
        assert(quantity >= 1) {
            "Purchase quantity cannot be less than 1"
        }
        assert(currency.length == 3 && currency.all { it.isLetter() }) {
            "Invalid currency code $currency. Supported ISO 4217 currency code only."
        }
        assert(price >= 0.0) {
            "Product price cannot be less than zero"
        }
    }

    /**
     * Adds Google Play purchase data required to validate the purchase.
     *
     * ```
     * CAS.reportPurchase(
     *  new PurchaseInfo(...)
     *      .setValidation(
     *          purchase.getOriginalJson(),
     *          purchase.getSignature()
     *      )
     * )
     * ```
     *
     * @param originalJson Original purchase JSON returned by Google Play.
     * @param signature Signature returned by Google Play.
     */
    fun setValidation(originalJson: String?, signature: String?) {
        this.json = originalJson.takeUnless { it.isNullOrEmpty() }
        this.signature = signature.takeUnless { it.isNullOrEmpty() }
    }

    /**
     * Adds subscription purchase data required to validate the subscription.
     *
     * ```
     * CAS.reportPurchase(
     *  new PurchaseInfo(...)
     *      .setSubsValidation(
     *          purchase.getOriginalJson(),
     *          purchase.getSignature(),
     *          purchase.getPurchaseToken(),
     *          purchase.getPurchaseTime()
     *      )
     * )
     * ```
     *
     * @param originalJson Original purchase JSON returned by Google Play.
     * @param signature Signature returned by Google Play.
     * @param purchaseToken Google Play purchase token. Can be null to get token from originalJson.
     * @param purchaseTime Subscription purchase time in milliseconds since Unix epoch. Can be 0 to get time from originalJson.
     * @return This [PurchaseInfo] instance, to allow chaining calls.
     */
    fun setSubsValidation(
        originalJson: String?,
        signature: String?,
        purchaseToken: String?,
        purchaseTime: Long
    ): PurchaseInfo {
        setValidation(originalJson, signature)
        this.subscriptionToken = purchaseToken.takeUnless { it.isNullOrEmpty() }
        this.subscriptionTime = purchaseTime.coerceAtLeast(0)
        return this
    }

    /**
     * Adds the required receipt ID and user ID data to validate an Amazon AppStore purchase,
     * and marks this [PurchaseInfo] as coming from the Amazon Appstore.
     *
     * The publisher is responsible for supplying the purchase price, currency, and quantity values (e.g. from their own
     * product catalog / pricing configuration) to ensure revenue is reported accurately.
     *
     * ```
     * CAS.reportPurchase(
     *  new PurchaseInfo(...)
     *      .setAmazonValidation(
     *          receipt.getReceiptId(),
     *          userData.getUserId()
     *      )
     * )
     * ```
     *
     * @param receiptId Amazon receipt identifier from the Amazon IAP Receipt.
     * @param userId Amazon user identifier.
     * @return This [PurchaseInfo] instance, to allow chaining calls.
     */
    fun setAmazonValidation(
        receiptId: String,
        userId: String?
    ): PurchaseInfo {
        provider = PROVIDER_AMAZON_APPSTORE
        signature = receiptId
        this.userId = userId
        return this
    }

    /**
     * Adds the required receipt ID and user ID data to validate an Amazon AppStore purchase,
     * and marks this [PurchaseInfo] as coming from the Amazon Appstore.
     *
     * The publisher is responsible for supplying the purchase price, currency, and quantity values (e.g. from their own
     * product catalog / pricing configuration) to ensure revenue is reported accurately.
     *
     * ```
     * CAS.reportPurchase(
     *  new PurchaseInfo(...)
     *      .setAmazonValidation(receipt.toString())
     * )
     * ```
     *
     * @param receipt JSON string of the Amazon IAP Receipt.
     * @return This [PurchaseInfo] instance, to allow chaining calls.
     */
    fun setAmazonValidation(receipt: String): PurchaseInfo = try {
        val json = JSONObject(receipt)
        setAmazonValidation(
            json.getString("receiptId"),
            json.getStringOrNull("userId")
        )
    } catch (tr: Throwable) {
        Session.debug("Purchase Amazon validation failed", tr)
        this
    }

    /**
     * Adds the required Order ID data to validate an Xsolla purchase,
     * and marks this [PurchaseInfo] as coming from the Xsolla.
     *
     * @param orderId Xsolla order id.
     * @param userId Xsolla user id.
     * @return This [PurchaseInfo] instance, to allow chaining calls.
     */
    fun setXsollaValidation(orderId: String, userId: String?): PurchaseInfo {
        provider = PROVIDER_XSOLLA
        signature = orderId
        this.userId = userId
        return this
    }

    constructor(other: PurchaseInfo) : this(
        other.productId,
        other.price,
        other.currency,
        other.quantity,
    ) {
        json = other.json
        signature = other.signature
        userId = other.userId
        subscriptionToken = other.subscriptionToken
        subscriptionTime = other.subscriptionTime
        provider = other.provider
    }

    companion object {
        /** Google Play Billing. */
        const val PROVIDER_GOOGLE_BILLING = "google"

        /** Amazon Appstore IAP. */
        const val PROVIDER_AMAZON_APPSTORE = "amazon"

        /** Xsolla payment provider. */
        const val PROVIDER_XSOLLA = "xsolla"
    }
}