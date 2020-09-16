package com.ahsailabs.beritakita;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ahsailabs.beritakita.configs.Config;
import com.ahsailabs.beritakita.ui.detail.DetailViewModel;
import com.ahsailabs.beritakita.ui.detail.models.NewsDetail;
import com.ahsailabs.beritakita.ui.detail.models.NewsDetailResponse;
import com.ahsailabs.beritakita.utils.HttpUtil;
import com.ahsailabs.beritakita.utils.InfoUtil;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.gsonparserfactory.GsonParserFactory;
import com.androidnetworking.interfaces.ParsedRequestListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ahsailabs.beritakita.R;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private static final String PARAM_NEWS_ID = "param_news_id";
    private TextView tvTitle;
    private TextView tvUser;
    private TextView tvDate;
    private MaterialTextView tvBody;
    private ImageView ivPhoto;
    private ScrollView svMain;

    private LinearLayout llLoadingPanel;
    private ProgressBar pbLoadingIndicator;
    private DetailViewModel detailViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadViews();

        detailViewModel = new ViewModelProvider(this).get(DetailViewModel.class);

        if(savedInstanceState == null || detailViewModel.newsDetail==null) {
            loadData();
        } else {
            updateView();
        }

    }

    private void loadData() {
        showLoading();
        String newsId = getIntent().getStringExtra(PARAM_NEWS_ID);
        AndroidNetworking.get(Config.getNewsDetailUrl().replace("{id}", newsId))
                .setOkHttpClient(HttpUtil.getCLient(this))
                .setTag("newsdetail")
                .setPriority(Priority.HIGH)
                .build()
                .getAsObject(NewsDetailResponse.class, new ParsedRequestListener<NewsDetailResponse>() {
                    @Override
                    public void onResponse(NewsDetailResponse response) {
                        if(response.getStatus() == 1){
                            //show detail in views
                            detailViewModel.newsDetail = response.getNewsDetail();
                            updateView();
                        } else {
                            InfoUtil.showToast(DetailActivity.this, response.getMessage());
                        }
                        hideLoading();
                    }

                    @Override
                    public void onError(ANError anError) {
                        InfoUtil.showToast(DetailActivity.this, anError.getMessage());
                        hideLoading();
                    }
                });
    }

    private void updateView() {
        tvTitle.setText(detailViewModel.newsDetail.getTitle());
        getSupportActionBar().setTitle(detailViewModel.newsDetail.getTitle());

        tvDate.setText(detailViewModel.newsDetail.getCreatedAt());
        tvUser.setText(detailViewModel.newsDetail.getCreatedBy());
        tvBody.setText(detailViewModel.newsDetail.getBody());

        if(!TextUtils.isEmpty(detailViewModel.newsDetail.getPhoto())){
            Picasso.get().load(detailViewModel.newsDetail.getPhoto()).into(ivPhoto);
        }
    }

    private void showLoading(){
        svMain.setVisibility(View.GONE);
        llLoadingPanel.setVisibility(View.VISIBLE);
        pbLoadingIndicator.setProgress(50);
    }

    private void hideLoading(){
        svMain.setVisibility(View.VISIBLE);
        llLoadingPanel.setVisibility(View.GONE);
        pbLoadingIndicator.setProgress(0);
    }

    private void loadViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvUser = findViewById(R.id.tvUser);
        tvDate = findViewById(R.id.tvDate);
        tvBody = findViewById(R.id.tvBody);
        ivPhoto = findViewById(R.id.ivPhoto);
        svMain = findViewById(R.id.svMain);

        llLoadingPanel = findViewById(R.id.llLoadingPanel);
        pbLoadingIndicator = findViewById(R.id.pbLoadingIndicator);
    }

    @Override
    protected void onDestroy() {
        AndroidNetworking.cancel("newsdetail");
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static void start(Context context, String newsId){
        Intent detailIntent = new Intent(context, DetailActivity.class);
        detailIntent.putExtra(PARAM_NEWS_ID, newsId);
        context.startActivity(detailIntent);
    }
}