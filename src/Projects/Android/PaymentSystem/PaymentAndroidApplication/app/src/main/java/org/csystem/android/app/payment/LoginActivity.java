package org.csystem.android.app.payment;

import static org.csystem.android.app.payment.global.key.BundleKeyKt.LOGIN_INFO;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.karandev.util.data.service.DataServiceException;

import org.csystem.android.app.payment.data.service.PaymentApplicationDataService;
import org.csystem.android.app.payment.data.service.dto.LoginInfoSaveDTO;
import org.csystem.android.app.payment.databinding.ActivityLoginBinding;
import org.csystem.android.app.payment.viewmodel.LoginActivityListenerViewModel;

import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding m_binding;

    @Inject
    PaymentApplicationDataService datService;

    @Inject
    ScheduledExecutorService threadPool;

    private void saveLoginInfoUIThreadCallback(LoginInfoSaveDTO loginInfo)
    {
        Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, OperationsActivity.class).putExtra(LOGIN_INFO, loginInfo));
    }

    private void loginButtonClickedCallback()
    {
        try {
            var loginInfo = m_binding.getLoginInfo();

            if (datService.checkAndSaveLoginInfo(loginInfo))
                runOnUiThread(() -> saveLoginInfoUIThreadCallback(loginInfo));
            else
                runOnUiThread(() -> Toast.makeText(this, "Access denied!...", Toast.LENGTH_SHORT).show());
        }
        catch (DataServiceException ex) {
            runOnUiThread(() -> Toast.makeText(this, "Data problem:" + ex.getMessage(), Toast.LENGTH_LONG).show());
        }
        catch (Throwable ignore) {
            runOnUiThread(() -> Toast.makeText(this, "Problem occurred. Try again later", Toast.LENGTH_LONG).show());
        }
    }

    private void initBinding()
    {
        m_binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        m_binding.setViewModel(new LoginActivityListenerViewModel(this));
        m_binding.setLoginInfo(new LoginInfoSaveDTO());
    }

    private void init()
    {
        initBinding();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init();
    }

    public void loginButtonClicked()
    {
        threadPool.execute(this::loginButtonClickedCallback);
    }
}