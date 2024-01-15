package org.csystem.android.app.payment

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.karandev.util.data.service.DataServiceException
import dagger.hilt.android.AndroidEntryPoint
import org.csystem.android.app.payment.data.service.PaymentApplicationDataService
import org.csystem.android.app.payment.data.service.dto.LoginInfoDTO
import org.csystem.android.app.payment.data.service.dto.LoginInfoSaveDTO
import org.csystem.android.app.payment.databinding.ActivityLoginInformationBinding
import org.csystem.android.app.payment.global.getLoginInfo
import org.csystem.android.app.payment.viewmodel.LoginInformationActivityListenerViewModel
import java.util.concurrent.ScheduledExecutorService
import javax.inject.Inject

@AndroidEntryPoint
class LoginInformationActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityLoginInformationBinding
    private lateinit var mLoginInfo: LoginInfoSaveDTO

    @Inject
    lateinit var dataService: PaymentApplicationDataService

    @Inject
    lateinit var threadPool: ScheduledExecutorService

    private fun successLoginsButtonClickedCallback()
    {
        try {
            runOnUiThread{mBinding.adapter!!.clear()}
            dataService.findSuccessLoginInfoByUserName(mLoginInfo.username)
                .forEach { runOnUiThread{mBinding.adapter!!.add(it)} }
        }
        catch (ex: DataServiceException) {
            runOnUiThread{Toast.makeText(this, "Data problem:${ex.message}", Toast.LENGTH_LONG).show()}
        }
        catch (ignore: Throwable) {
            runOnUiThread{Toast.makeText(this, "Problem occurred. Try again later", Toast.LENGTH_LONG).show()}
        }
    }

    private fun failLoginsButtonClickedCallback()
    {
        try {
            runOnUiThread{mBinding.adapter!!.clear()}
            val logins = dataService.findFailLoginInfoByUserName(mLoginInfo.username)

            if (logins.isNotEmpty())
                runOnUiThread{logins.forEach { mBinding.adapter!!.add(it) }}
            else
                runOnUiThread{Toast.makeText(this, "No fail login", Toast.LENGTH_LONG).show()}
        }
        catch (ex: DataServiceException) {
            runOnUiThread{Toast.makeText(this, "Data problem:${ex.message}", Toast.LENGTH_LONG).show()}
        }
        catch (ignore: Throwable) {
            runOnUiThread{Toast.makeText(this, "Problem occurred. Try again later", Toast.LENGTH_LONG).show()}
        }
    }

    private fun initLoginInfo()
    {
        mLoginInfo =  getLoginInfo(intent)
    }

    private fun initBinding()
    {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_login_information)
        mBinding.viewModel = LoginInformationActivityListenerViewModel(this)
        mBinding.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<LoginInfoDTO>())
    }

    private fun initialize()
    {
        initBinding()
        initLoginInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        initialize()
    }

    fun successLoginsButtonClicked() = threadPool.execute{successLoginsButtonClickedCallback()}

    fun failLoginsButtonClicked() = threadPool.execute{failLoginsButtonClickedCallback()}

    fun loginInformationItemClicked(pos: Int)
    {
        val loginInfoStatusDTO = mBinding.adapter!!.getItem(pos)

        AlertDialog.Builder(this)
            .setTitle(R.string.alertdialog_login_info_title_text)
            .setMessage(loginInfoStatusDTO.toString())
            .setPositiveButton(R.string.alertdialog_login_info_ok_button_text) {_, _->}
            .create().show()
    }

    fun closeButtonClicked()
    {
        finish()
    }
}