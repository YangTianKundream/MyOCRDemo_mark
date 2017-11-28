package com.mark.demo.myocrdemo_mark;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.mark.demo.myocrdemo_mark.utils.FileUtils;
import com.mark.demo.myocrdemo_mark.utils.ToastUtils;

import java.io.File;

/**
 * 文字识别 :http://ai.baidu.com/tech/ocr
 * */
public class MarkActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "MarkActivity";
    private static final String API_KEY = "ybCCSmm2IaltZeYWbCrpCGsU";
    private static final String SECRET_KEY = "hNbt7wIMCpmF27lOaEjuwfr8S1jI5KtB";
    private static final int REQUEST_CODE_BANKCARD = 100; //银行卡的请求码
    private static final int REQUEST_CODE_DRIVING_LICENSE = 101; //驾驶证的请求码
    private static final int REQUEST_CODE_CAR_CARD = 102;//车牌号的请求码

    private TextView resultTv;

    //百度AI开放平台使用OAuth2.0授权调用开放API，调用API时必须在URL中带上accesss_token参数。AccessToken可用AK/SK或者授权文件的方式获得。
    private boolean hasGotToken = false; //是否已经获取到了Token
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark);

        mContext = this;
        initView();

        // 请选择您的初始化方式
        initAccessToken();  //授权文件、安全模式
//        initAccessTokenWithAkSk();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放内存资源
        OCR.getInstance().release();
    }

    //初始化View
    private void initView() {
        findViewById(R.id.id_card_btn).setOnClickListener(this);
        findViewById(R.id.bankcard_btn).setOnClickListener(this);
        findViewById(R.id.driving_license_btn).setOnClickListener(this);
        findViewById(R.id.car_card_btn).setOnClickListener(this);
        resultTv = (TextView) findViewById(R.id.info_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_card_btn:
                if (hasGotToken){
                    //身份证
                    startActivity(new Intent(mContext, IDCardActivity.class));
                }
                break;
            case R.id.bankcard_btn:
                if (hasGotToken){
                    //银行卡
                    scanBank();
                }
                break;
            case R.id.driving_license_btn:
                if (hasGotToken){
                    //驾驶证
                    scanDrivingLicense();
                }
                break;
            case R.id.car_card_btn:
                if (hasGotToken){
                    //车牌号识别
                    scanCarCard();
                }
                break;
        }
    }

    //授权文件（安全模式）
    //此种身份验证方案使用授权文件获得AccessToken，缓存在本地。建议有安全考虑的开发者使用此种身份验证方式。
    private void initAccessToken() {
        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                // 调用成功，返回AccessToken对象
                String token = accessToken.getAccessToken();
                Log.i(TAG, "token:-------->" + token);
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Log.i(TAG, "onError:licence方式获取token失败---->" + error.getMessage());
                ToastUtils.showToast(mContext, "licence方式获取token失败  " + error.getMessage());
            }
        }, getApplicationContext());
    }

    //通过AK/SK的方式获得AccessToken。
    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                // 调用成功，返回AccessToken对象
                String token = result.getAccessToken();
                Log.i(TAG, "token:-------->" + token);
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                Log.i(TAG, "onError:AK，SK方式获取token失败---->" + error.getMessage());
                ToastUtils.showToast(mContext, "AK，SK方式获取token失败" + error.getMessage());
            }
        }, getApplicationContext(), API_KEY, SECRET_KEY);
    }

    //扫描银行卡
    private void scanBank() {
        // 调用拍摄银行卡的activity
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_BANK_CARD);
        startActivityForResult(intent, REQUEST_CODE_BANKCARD);
    }

    /**
     * 扫描车牌号
     * */
    private void scanCarCard() {
        Intent intent = new Intent(getApplicationContext(),CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent,REQUEST_CODE_CAR_CARD);
    }

    //扫描驾驶证
    private void scanDrivingLicense() {
        Intent intent = new Intent(MarkActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtils.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
                CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE_DRIVING_LICENSE);
    }

    /**
     * 银行卡的识别
     *
     * @param filePath
     */
    public void recBankCard(String filePath) {
        BankCardParams param = new BankCardParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance().recognizeBankCard(param, new OnResultListener<BankCardResult>() {
            @Override
            public void onResult(BankCardResult result) {
                String bankCardNumber = result.getBankCardNumber(); //卡号
                String bankName = result.getBankName(); //开户银行名称
                String type = result.getBankCardType().toString(); //银行卡类型
                Log.i(TAG, "bankCardNumber:---->" + bankCardNumber);
                Log.i(TAG, "bankName:---->" + bankName);
                Log.i(TAG, "type:---->" + type);
                String str = "银行卡号：" + bankCardNumber + "\n" + "开户行名称：" + bankName + "\n" + "银行卡类型：" + type;
                resultTv.setText(str);
            }

            @Override
            public void onError(OCRError error) {
                Log.i(TAG, "onError: 扫描银行卡错误  " + error.getMessage());
            }
        });
    }

    /**
     * 驾驶证的识别
     * */
    public void recDrivingLicense(String filePath) {
        OcrRequestParams param = new OcrRequestParams();
        param.setImageFile(new File(filePath));
        OCR.getInstance().recognizeDrivingLicense(param, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult result) {
                if (result != null) {
                    Log.i(TAG, "onResult: 扫描驾驶证成功");
                    String jsonRes = result.getJsonRes();
                    Log.i(TAG, "jsonRes: " + jsonRes);
                    String str = "驾驶证信息:" + jsonRes;
                    resultTv.setText(str);
                }
            }

            @Override
            public void onError(OCRError error) {
                Log.i(TAG, "onError: 扫描驾驶证错误  " + error.getMessage());
            }
        });
    }

    /**
     * 车牌号的识别
     * @param filePath
     */
    private void recCarCard(String filePath) {
        OcrRequestParams params = new OcrRequestParams();
        params.setImageFile(new File(filePath));
        OCR.getInstance().recognizeLicensePlate(params, new OnResultListener<OcrResponseResult>() {
            @Override
            public void onResult(OcrResponseResult ocrResponseResult) {
                if (ocrResponseResult != null){
                    Log.i(TAG, "onResult:扫描车牌号成功");
                    String jsonRes = ocrResponseResult.getJsonRes();
                    Log.i(TAG, "jsonRes: " + jsonRes);
                    String str = "车牌号信息:" + jsonRes;
                    resultTv.setText(str);
                }
            }

            @Override
            public void onError(OCRError ocrError) {
                Log.i(TAG, "onError: 扫描车牌号错误  " + ocrError.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //拍摄类型:银行卡
        if (requestCode == REQUEST_CODE_BANKCARD && resultCode == Activity.RESULT_OK) {
            Log.e("mark",FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
            recBankCard(FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
        }
        //拍摄类型:驾驶证
        if (requestCode == REQUEST_CODE_DRIVING_LICENSE && resultCode == Activity.RESULT_OK) {
            Log.e("mark",FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
            recDrivingLicense(FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
        }
        //拍摄类型：车牌号
        if (requestCode == REQUEST_CODE_CAR_CARD && resultCode == Activity.RESULT_OK){
            Log.e("mark",FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
            recCarCard(FileUtils.getSaveFile(getApplicationContext()).getAbsolutePath());
        }
    }

}
