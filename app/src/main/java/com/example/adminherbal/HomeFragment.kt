package com.example.adminherbal

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filterable
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminherbal.adapter.AdapterProduct
import com.example.adminherbal.adapter.CategoriesAdapter
import com.example.adminherbal.databinding.EditProductLayoutBinding
import com.example.adminherbal.databinding.FragmentHomeBinding
import com.example.adminherbal.models.Categories
import com.example.adminherbal.models.Product
import com.example.adminherbal.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import kotlin.reflect.KMutableProperty1
import com.example.adminherbal.adapter.AdapterProduct as AdapterProduct1

class HomeFragment : Fragment() {
    val viewModel: AdminViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        getAllTheProducts("All")
        setCategories()

        return binding.root
    }



    private fun getAllTheProducts(category: String) {
        binding.shimmerViewContainer.visibility = View.VISIBLE
        lifecycleScope.launch {
            viewModel.fetchAllTheProducts(category).collect {
                if (it.isEmpty()) {
                    binding.rvProducts.visibility = View.GONE
                    binding.tvText.visibility = View.VISIBLE
                } else {
                    binding.rvProducts.visibility = View.VISIBLE
                    binding.tvText.visibility = View.GONE
                }
                val adapterProduct = AdapterProduct(::onEditButtonClicked)
                binding.rvProducts.adapter = adapterProduct
                adapterProduct.differ.submitList(it)
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }
    }

    private fun setCategories() {
        val categoryList = ArrayList<Categories>()
        for (i in 0 until Constants.allProductsCategoryIcons.size) {
            categoryList.add(
                Categories(
                    Constants.allProductsCategory[i],
                    Constants.allProductsCategoryIcons[i]
                )
            )
        }
        binding.rvCategories.adapter = CategoriesAdapter(categoryList, ::onCategoryClicked)
    }

    private fun onCategoryClicked(categories: Categories) {
        getAllTheProducts(categories.category)
    }

    private fun onEditButtonClicked(product: Product) {
        val editProduct =
            EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProduct.apply {
            etProductTitle.setText(product.productTitle)
            etProductCategory.setText(product.productCategory)
            etProductPrice.setText(product.productPrice.toString())
            etProductType.setText(product.productType)
            etProductQuantity.setText(product.productQuantity.toString())
            etProductStock.setText(product.productStock.toString())
            etProductUnit.setText(product.productUnit)
        }
        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProduct.root)
            .create()
        alertDialog.show()

        editProduct.btnEdit.setOnClickListener {
            editProduct.etProductTitle.isEnabled = true
            editProduct.etProductCategory.isEnabled = true
            editProduct.etProductPrice.isEnabled = true
            editProduct.etProductQuantity.isEnabled = true
            editProduct.etProductType.isEnabled = true
            editProduct.etProductStock.isEnabled = true
            editProduct.etProductUnit.isEnabled = true
        }
        // Call setAutoCompleteTextViews here
        setAutoCompleteTextViews(editProduct)
        editProduct.btnSave.setOnClickListener{
            lifecycleScope.launch {
                product.productTitle= editProduct.etProductTitle.text.toString()
                product.productQuantity=editProduct.etProductQuantity.text.toString().toInt()
                product.productPrice=editProduct.etProductPrice.text.toString().toInt()
                product.productUnit=editProduct.etProductUnit.text.toString()
                product.productStock=editProduct.etProductStock.text.toString().toInt()
                product.productCategory=editProduct.etProductCategory.text.toString()
                product.productType=editProduct.etProductType.text.toString()
                viewModel.savingUpdatedProducts(product)
            }

            Utils.showToast(requireContext(),"Saved changes!")
            alertDialog.dismiss()
        }
    }

    private fun setAutoCompleteTextViews(editProduct: EditProductLayoutBinding) {
        val units = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allUnitOfProducts)
        val category = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductsCategory)
        val productType = ArrayAdapter(requireContext(), R.layout.show_list, Constants.allProductType)

        editProduct.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }
    }
}