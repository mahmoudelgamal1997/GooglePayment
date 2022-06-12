package com.example.googlepayment


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.example.googlepayment.databinding.FragmentFirstBinding
import java.util.*


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), PurchasesUpdatedListener {

    private var _binding: FragmentFirstBinding? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var billingClient: BillingClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        billingClient = BillingClient.newBuilder(requireContext())
            .setListener { billingResult: BillingResult, list: List<Purchase?>? ->
                if (billingResult.responseCode == BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        Log.d("TestA2d", "" + list)
                        verifyPayment(purchase!!)

                        /**
                         * after subscribe successfully
                         */

                    }
                }
            }
            .enablePendingPurchases()
            .build()


        binding.purchaseButton.setOnClickListener({

            connectGooglePlayBilling()
        })

        //item Purchased

    }



    //initiate purchase on button click

    fun connectGooglePlayBilling() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                connectGooglePlayBilling()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingResponseCode.OK) {
                    getProducts()
                }
            }
        })
    }

    fun getProducts() {
        val products: MutableList<String> = ArrayList()
        products.add("forex30")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(products).setType(BillingClient.SkuType.SUBS)
        billingClient.querySkuDetailsAsync(
            params.build()
        ) { billingResult: BillingResult, list: List<SkuDetails>? ->
            if (billingResult.responseCode == BillingResponseCode.OK && list != null) {
                for (skuDetails in list) {
                    if (skuDetails.sku == "forex30") {
                        launchPurchaseFlow(skuDetails)
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(skuDetails: SkuDetails?) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails!!).setObfuscatedAccountId("id")
            .build()
        billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
    }

    fun verifyPayment(purchase: Purchase) {
        billingClient.consumeAsync(
            ConsumeParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
        ) { billingResult: BillingResult, s: String? ->
            if (billingResult.responseCode == BillingResponseCode.OK) {
                Log.e("TestA2d", "Item consumed")
                Toast.makeText(context, "Item Consumed", Toast.LENGTH_SHORT).show()
                if (purchase.skus[0] == "forex30") {
                    Log.e("Verify payment", "verify payment")
                }
            }
        }
    }


    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.d("TAG", "onPurchasesUpdated: " + billingResult.responseCode)
        if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
            val calendara = Calendar.getInstance()
            val year = calendara[Calendar.YEAR]
            val month = calendara[Calendar.MONTH] + 1
            val day = calendara[Calendar.DAY_OF_MONTH]
            val date = "$year-$month-$day"
            Log.e("TAG", "onPurchasesUpdated " + purchases[0].originalJson)

               //do any thing like call api server
        } else {
            Log.e("TAG", "onPurchasesUpdated cancelled")
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        if (billingClient != null) {
            billingClient!!.endConnection()
        }
    }

    companion object {
        const val PREF_FILE = "MyPref"
        const val PURCHASE_KEY = "forex30"
        const val PRODUCT_ID = "forex30"
    }
}