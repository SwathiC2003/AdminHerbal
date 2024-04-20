package com.example.adminherbal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.adminherbal.databinding.FragmentOTPBinding
import com.example.adminherbal.models.Admin
import com.example.adminherbal.viewmodels.AuthViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {
    private val viewModel : AuthViewModel by viewModels()
    private lateinit var binding: FragmentOTPBinding
    private lateinit var userNumber : String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentOTPBinding.inflate(layoutInflater)

        getUserNUmber()
        customizingEnteringOTP()
        sendOTP()
        onLoginButtonClicked()
        onBackButtonClicked()
        return binding.root
    }

    private fun onLoginButtonClicked(){
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(),"Signing you....")
            val editTexts= arrayOf(binding.et0tp1,binding.et0tp2,binding.et0tp3,binding.et0tp4,binding.et0tp5,binding.et0tp6)
            val otp = editTexts.joinToString (""){it.text.toString()}

            if (otp.length < editTexts.size){
                Utils.showToast(requireContext(),"Please enter the right otp ")
            }
            else{
                editTexts.forEach {it.text?.clear() ; it.clearFocus() }
                verifyOtp(otp)
            }

        }
    }

    private fun verifyOtp(otp:String){
        val admin = Admin(uid = null, adminPhoneNumber = userNumber)
        viewModel.signInWithPhoneAuthCredential(otp,userNumber, admin)
        lifecycleScope.launch {
            viewModel.isSignedInSuccessfully.collect{
                if(it){
                    Utils.hideDialog()
                    Utils.showToast(requireContext(),"Logged In...")
                    startActivity(Intent(requireActivity(), AdminMainActivity::class.java))
                    requireActivity().finish()
                }
            }
        }


    }

    private fun sendOTP(){
        Utils.showDialog(requireContext(),"Sending OTP....")
        viewModel.apply {
            sendOTP(userNumber, requireActivity())
            lifecycleScope.launch {
                otpSent.collect{otpSent ->
                    if (otpSent){
                        Utils.hideDialog()
                        Utils.showToast(requireContext(),"otp sent to the number...")
                    }
                }

            }

        }


    }

    private fun onBackButtonClicked(){
        binding.tb0tpFragment.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
        }
    }

    private fun customizingEnteringOTP(){
        val editTexts= arrayOf(binding.et0tp1,binding.et0tp2,binding.et0tp3,binding.et0tp4,binding.et0tp5,binding.et0tp6)
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1){
                        if(i<editTexts.size-1) {
                            editTexts[i+1].requestFocus()
                        }
                    }else if (s?.length == 0){
                        if(i>0){
                            editTexts[i-1].requestFocus()
                        }
                    }   }



            })
        }
    }

    private fun getUserNUmber(){
        val bundle = arguments
        userNumber=bundle?.getString("number").toString()

        binding.tvUserNumber.text=userNumber
    }


}