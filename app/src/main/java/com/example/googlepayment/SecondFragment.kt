package com.example.googlepayment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.example.googlepayment.databinding.FragmentSecondBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private var billingClient: BillingClient? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


            billingClient = BillingClient.newBuilder(requireContext())
                .enablePendingPurchases()
                .setListener { billingResult, list ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                        for (purchase in list) {
                            verifySubPurchase(purchase)
                        }
                    }
                }.build()

            //start the connection after initializing the billing client

            //start the connection after initializing the billing client
            establishConnection()




    }



    fun establishConnection() {
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@NonNull billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
            }
        })
    }


    fun showProducts() {
        val skuList: MutableList<String> = ArrayList()
        skuList.add("sub_premium")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingClient!!.querySkuDetailsAsync(
            params.build()
        ) { billingResult, skuDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                // Process the result.
                for (skuDetails in skuDetailsList) {
                    if (skuDetails.sku == "sub_premium") {
                        //Now update the UI
                        binding.buttonSecond.setText(skuDetails.price + " Per Month")
                        binding.buttonSecond.setOnClickListener { view ->
                            Log.e("clicked","clicked")
                            launchPurchaseFlow(skuDetails) }
                    }
                }
            }
        }
    }

    fun launchPurchaseFlow(skuDetails: SkuDetails?) {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails!!)
            .build()
        billingClient!!.launchBillingFlow(requireActivity(), billingFlowParams)
    }


    fun verifySubPurchase(purchases: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient!!.acknowledgePurchase(
            acknowledgePurchaseParams
        ) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                //Toast.makeText(SubscriptionActivity.this, "Item Consumed", Toast.LENGTH_SHORT).show();
                // Handle the success of the consume operation.
                //user prefs to set premium
                Toast.makeText(requireActivity(), "You are a premium user now", Toast.LENGTH_SHORT)
                    .show()
                //updateUser();

                //Setting premium to 1
                // 1 - premium
                //0 - no premium
        //        prefs.setPremium(1)
            }
        }
        Log.d("Main", "Purchase Token: " + purchases.purchaseToken)
        Log.d("Main", "Purchase Time: " + purchases.purchaseTime)
        Log.d("Main", "Purchase OrderID: " + purchases.orderId)
    }

    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient?.acknowledgePurchase(
                    params
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
                    //    _isNewPurchaseAcknowledged.value = true
                    }
                }
            }
        }
    }
    fun terminateBillingConnection() {
        Log.i("TAG", "Terminating connection")
        billingClient?.endConnection()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}