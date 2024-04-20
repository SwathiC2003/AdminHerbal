package com.example.adminherbal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.adminherbal.adapter.AdapterSelectedImage
import com.example.adminherbal.databinding.FragmentAddProductBinding
import com.example.adminherbal.models.Product
import com.example.adminherbal.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class AddProductFragment : Fragment() {

    private val viewModel:AdminViewModel by viewModels()
    private lateinit var binding: FragmentAddProductBinding
    private val imageUris : ArrayList<Uri> = arrayListOf()
    val selectedImage =  registerForActivityResult(ActivityResultContracts.GetMultipleContents()){listOfUri->
        val fiveImages=listOfUri.take(5)
        imageUris.clear()
        imageUris.addAll(fiveImages)

        binding.rvProductImages.adapter=AdapterSelectedImage(imageUris)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setAutoCompleteTextViews()
        onImageSelectClicked()
        onAddButtonClicked()
        return binding.root
    }

    private fun onAddButtonClicked() {
       binding.btnAddProduct.setOnClickListener(){
           Utils.showDialog(requireContext(), "Uploading images....")
           val productTitle=binding.etProductTitle.text.toString()
           val productQuantity=binding.etProductQuantity.text.toString()
           val productUnit=binding.etProductUnit.text.toString()
           val productPrice=binding.etProductPrice.text.toString()
           val productStock=binding.etProductStock.text.toString()
           val productCategory=binding.etProductCategory.text.toString()
           val productType=binding.etProductType.text.toString()

           if(productTitle.isEmpty()||productQuantity.isEmpty()||productUnit.isEmpty()||productUnit.isEmpty()||
               productPrice.isEmpty()||productStock.isEmpty()||productCategory.isEmpty()||productType.isEmpty()){
               Utils.apply {
                   hideDialog()
                   showToast(requireContext(), "Empty fields not allowed")
               }

           }
       else if (imageUris.isEmpty()){
               Utils.apply {
                   hideDialog()
                   showToast(requireContext(), "Please Upload Some Image")
               }
           }
           else{
               val product= Product(
                   productTitle=productTitle,
                   productQuantity = productQuantity.toInt(),
                   productUnit = productUnit,
                   productPrice = productPrice.toInt(),
                   productStock = productStock.toInt(),
                   productCategory = productCategory,
                   productType = productType,
                   itemCount=0,
                   adminUid= Utils.getCurrentUserId(),
                   productRandomId = Utils.getRandomId()
               )
               saveImage(product)
           }

       }

    }

    private fun saveImage(product: Product) {
        viewModel.saveImageInDB(imageUris)
        lifecycleScope.launch {
            viewModel.isImageUploaded.collect{
                if(it){
                    Utils.apply {
                        hideDialog()
                        showToast(requireContext(), "image saved")
                    }

                    getUrls(product)
                }
            }
        }

    }

    private fun getUrls(product: Product) {
        Utils.showDialog(requireContext(),"Publishing Product...")
        lifecycleScope.launch {
            viewModel.downloadedUrls.collect{
                val urls = it
                product.productImageUris=urls
                saveProduct(product)
            }
        }
    }

    private  fun saveProduct(product: Product) {
        viewModel.saveProduct(product)
        lifecycleScope.launch{
            viewModel.isProductSaved.collect(){
                if(it){
                    Utils.hideDialog()
                    startActivity(Intent(requireContext(),AdminMainActivity::class.java))
                    Utils.showToast(requireContext(),"Your Product is Live")
                }
        }
        }
    }


    private fun onImageSelectClicked() {
        binding.btnSelectImage.setOnClickListener{
            selectedImage.launch("image/*")
        }
    }

    private fun setAutoCompleteTextViews() {
        val units = ArrayAdapter(requireContext(),R.layout.show_list, Constants.allUnitOfProducts)
        val category = ArrayAdapter(requireContext(),R.layout.show_list, Constants.allProductsCategory)
        val productType = ArrayAdapter(requireContext(),R.layout.show_list, Constants.allProductType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
        }

    }



}