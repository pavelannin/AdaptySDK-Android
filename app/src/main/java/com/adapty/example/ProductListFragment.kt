package com.adapty.example

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.adapty.Adapty
import com.adapty.example.adapter.ProductAdapter
import com.adapty.models.ProductModel
import kotlinx.android.synthetic.main.fragment_list.*

class ProductListFragment : Fragment(R.layout.fragment_list) {

    companion object {
        fun newInstance(products: List<ProductModel>) = ProductListFragment().apply {
            productList = products
        }
    }

    private var productList = listOf<ProductModel>()

    private val progressDialog: ProgressDialog by lazy {
        ProgressDialog(context)
    }

    private val productAdapter: ProductAdapter by lazy {
        ProductAdapter(products = productList, onPurchaseClick = { product ->
            activity?.let {
                progressDialog.show()
                Adapty.makePurchase(it, product) { purchaserInfo, purchaseToken, googleValidationResult, product, error ->
                    progressDialog.dismiss()
                    showToast(error?.message ?: "Success")
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        list.adapter = productAdapter
    }
}